package com.trynite.chefyarnnn

import com.trynite.chefyarnnn.data.repository.isNewerAppVersion
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppUpdateRepositoryTest {
    @Test
    fun comparesDottedVersionsNumerically() {
        assertTrue(isNewerAppVersion("1.0.10", "1.0.9"))
        assertTrue(isNewerAppVersion("1.1.0", "1.0.99"))
        assertTrue(isNewerAppVersion("1.0.1", "1.0"))
        assertFalse(isNewerAppVersion("1.0.0", "1.0"))
        assertFalse(isNewerAppVersion("invalid", "1.0"))
    }
}
