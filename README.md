# ChefYarnnn

## Features

- **Explorer** — browse recipes and read detailed cooking instructions.
- **Anong Lutuin?** — scan pantry items for cooking ideas.
- **Chef Cindy** — chat with an AI chef for cooking help and inspiration.
- **Palengke** — manage a shopping checklist and track item prices and totals.
- **App updates** — check public GitHub Releases for new versions, then download and install updates with your approval.

## Backup AI setup (Cloudflare Workers free tier)

When Firebase AI Logic reports quota exhaustion, the app sends the request to a Cloudflare Worker.
The Worker verifies the Firebase Google sign-in token and calls Gemini using an API key stored as
a Cloudflare secret. The key must never be embedded in the Android app or committed to source
control. The Worker free tier has usage limits, and Google AI Studio may impose separate model
quotas or billing terms.

Install Node.js 22 or newer and create a free Cloudflare account. From `functions/`, run:

```sh
npm install
npx wrangler login
npx wrangler secret put GEMINI_API_KEY
npx wrangler deploy
```

Paste the AI Studio key only into Wrangler's secure secret prompt. After deployment, copy the
Worker URL printed by Wrangler. Set it in the project-root `gradle.properties` (which is
gitignored) and rebuild the app:

```properties
backupAiWorkerUrl=https://chefyarnnn-backup-ai.YOUR_ACCOUNT.workers.dev
```

Firebase Authentication must be configured for Google sign-in. Only Google-authenticated Firebase
users can use this endpoint. The app forwards pantry images to the backup provider as JPEGs.
