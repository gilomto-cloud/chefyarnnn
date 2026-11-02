const FIREBASE_PROJECT_ID = "chefyarnnn";
const FIREBASE_ISSUER = `https://securetoken.google.com/${FIREBASE_PROJECT_ID}`;
const FIREBASE_JWKS_URL =
  "https://www.googleapis.com/service_accounts/v1/jwk/securetoken@system.gserviceaccount.com";
const GEMINI_MODEL = "gemini-3.5-flash";
const MAX_PROMPT_LENGTH = 20_000;
const MAX_IMAGE_BASE64_LENGTH = 4_200_000;
const MAX_IMAGE_BYTES = 3 * 1024 * 1024;
const MAX_REQUEST_BYTES = 4_400_000;
const MAX_OUTPUT_TOKENS = 4096;

let cachedJwks;
let cachedJwksExpiresAt = 0;

export default {
  async fetch(request, env) {
    if (request.method !== "POST") {
      return jsonResponse({ error: "Use POST for backup AI requests." }, 405);
    }

    const contentLength = Number(request.headers.get("content-length") ?? 0);
    if (contentLength > MAX_REQUEST_BYTES) {
      return jsonResponse({ error: "The request is too large." }, 413);
    }

    try {
      const user = await verifyFirebaseIdToken(request.headers.get("authorization"));
      const body = await request.json();
      const { operation, prompt, imageBase64, imageMimeType } = body ?? {};

      if (operation !== "chat" && operation !== "recipe") {
        return jsonResponse({ error: "Unsupported AI operation." }, 400);
      }
      if (
        typeof prompt !== "string" ||
        prompt.trim().length === 0 ||
        prompt.length > MAX_PROMPT_LENGTH
      ) {
        return jsonResponse({ error: "The prompt is missing or too long." }, 400);
      }

      const parts = [{ text: prompt }];
      if (imageBase64 !== undefined || imageMimeType !== undefined) {
        if (
          operation !== "recipe" ||
          typeof imageBase64 !== "string" ||
          imageBase64.length === 0 ||
          imageBase64.length > MAX_IMAGE_BASE64_LENGTH ||
          imageMimeType !== "image/jpeg"
        ) {
          return jsonResponse({ error: "The backup image is invalid or too large." }, 400);
        }
        const imageBytes = decodeBase64(imageBase64);
        if (!imageBytes || imageBytes.byteLength > MAX_IMAGE_BYTES) {
          return jsonResponse({ error: "The backup image is invalid or too large." }, 400);
        }
        parts.push({
          inlineData: {
            mimeType: imageMimeType,
            data: imageBase64,
          },
        });
      }

      const geminiResponse = await fetch(
        `https://generativelanguage.googleapis.com/v1beta/models/${GEMINI_MODEL}:generateContent`,
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            "x-goog-api-key": env.GEMINI_API_KEY,
          },
          body: JSON.stringify({
            systemInstruction: {
              parts: [{
                text: operation === "chat"
                  ? "You are Chef Cindy, a practical Filipino cooking assistant. Reply warmly and concisely in Taglish."
                  : "You are Chef Cindy, a warm and expert Filipino home cook. Follow the user's output format exactly.",
              }],
            },
            contents: [{ role: "user", parts }],
            generationConfig: {
              maxOutputTokens: MAX_OUTPUT_TOKENS,
            },
          }),
          signal: AbortSignal.timeout(45_000),
        },
      );

      const result = await geminiResponse.json().catch(() => null);
      if (!geminiResponse.ok) {
        return jsonResponse(
          {
            error: geminiResponse.status === 429
              ? "The backup AI quota is currently exhausted."
              : "The backup AI could not complete the request.",
          },
          geminiResponse.status === 429 ? 429 : 502,
        );
      }

      const text = result?.candidates?.[0]?.content?.parts
        ?.map((part) => part.text ?? "")
        .join("")
        .trim();
      if (!text) {
        return jsonResponse({ error: "The backup AI returned no text." }, 502);
      }

      return jsonResponse({ text }, 200);
    } catch (error) {
      if (error instanceof AuthenticationError) {
        return jsonResponse({ error: "A valid Google-signed-in account is required." }, 401);
      }
      if (error instanceof SyntaxError) {
        return jsonResponse({ error: "The request body must be valid JSON." }, 400);
      }
      return jsonResponse({ error: "The backup AI request failed. Try again later." }, 502);
    }
  },
};

async function verifyFirebaseIdToken(authorization) {
  if (typeof authorization !== "string" || !authorization.startsWith("Bearer ")) {
    throw new AuthenticationError();
  }

  const token = authorization.slice("Bearer ".length);
  if (token.length > 10_000) throw new AuthenticationError();
  const segments = token.split(".");
  if (segments.length !== 3) throw new AuthenticationError();

  let header;
  let claims;
  try {
    header = JSON.parse(new TextDecoder().decode(decodeBase64Url(segments[0])));
    claims = JSON.parse(new TextDecoder().decode(decodeBase64Url(segments[1])));
  } catch {
    throw new AuthenticationError();
  }
  if (header.alg !== "RS256" || typeof header.kid !== "string") {
    throw new AuthenticationError();
  }

  const now = Math.floor(Date.now() / 1000);
  if (
    claims.aud !== FIREBASE_PROJECT_ID ||
    claims.iss !== FIREBASE_ISSUER ||
    typeof claims.sub !== "string" ||
    claims.sub.length === 0 ||
    claims.sub.length > 128 ||
    typeof claims.exp !== "number" ||
    claims.exp <= now ||
    typeof claims.iat !== "number" ||
    claims.iat > now + 60 ||
    claims.firebase?.sign_in_provider !== "google.com"
  ) {
    throw new AuthenticationError();
  }

  const jwks = await getFirebaseJwks();
  const jwk = jwks.keys.find((key) => key.kid === header.kid);
  if (!jwk) {
    cachedJwksExpiresAt = 0;
    throw new AuthenticationError();
  }

  let validSignature = false;
  try {
    const publicKey = await crypto.subtle.importKey(
      "jwk",
      jwk,
      { name: "RSASSA-PKCS1-v1_5", hash: "SHA-256" },
      false,
      ["verify"],
    );
    validSignature = await crypto.subtle.verify(
      "RSASSA-PKCS1-v1_5",
      publicKey,
      decodeBase64Url(segments[2]),
      new TextEncoder().encode(`${segments[0]}.${segments[1]}`),
    );
  } catch {
    throw new AuthenticationError();
  }
  if (!validSignature) throw new AuthenticationError();
  return claims.sub;
}

async function getFirebaseJwks() {
  if (cachedJwks && Date.now() < cachedJwksExpiresAt) return cachedJwks;

  const response = await fetch(FIREBASE_JWKS_URL);
  if (!response.ok) throw new Error("Could not retrieve Firebase signing keys.");
  const jwks = await response.json();
  const cacheControl = response.headers.get("cache-control") ?? "";
  const maxAge = Number(cacheControl.match(/max-age=(\d+)/)?.[1] ?? 3600);
  cachedJwks = jwks;
  cachedJwksExpiresAt = Date.now() + maxAge * 1000;
  return jwks;
}

function decodeBase64(value) {
  if (!/^[A-Za-z0-9+/]*={0,2}$/.test(value)) return null;
  try {
    const binary = atob(value);
    return Uint8Array.from(binary, (character) => character.charCodeAt(0));
  } catch {
    return null;
  }
}

function decodeBase64Url(value) {
  const base64 = value.replace(/-/g, "+").replace(/_/g, "/");
  return decodeBase64(base64.padEnd(Math.ceil(base64.length / 4) * 4, "="));
}

function jsonResponse(body, status) {
  return new Response(JSON.stringify(body), {
    status,
    headers: { "Content-Type": "application/json; charset=utf-8" },
  });
}

class AuthenticationError extends Error {}
