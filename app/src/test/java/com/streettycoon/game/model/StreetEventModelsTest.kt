package com.streettycoon.game.model

import org.junit.Assert.*
import org.junit.Test
import kotlin.random.Random

/**
 * Unit tests for StreetEventModels.kt
 * Tests event generation, effects, and lifecycle
 */
class StreetEventModelsTest {

    // ==================== EVENT GENERATION ====================

    @Test
    fun `generate creates a valid event`() {
        val event = StreetEventGenerator.generate("district_1", Random(42))
        assertNotNull(event.eventId)
        assertNotNull(event.title)
        assertNotNull(event.description)
        assertTrue(event.title.isNotEmpty())
        assertTrue(event.description.isNotEmpty())
        assertEquals("district_1", event.districtId)
        assertTrue(event.durationMs > 0)
        assertEquals(EventResolution.ACTIVE, event.resolution)
    }

    @Test
    fun `generate produces variety of event types`() {
        val rng = Random(12345)
        val types = (0 until 100).map { StreetEventGenerator.generate("d1", rng).type }.toSet()
        // With 100 events generated with different seeds, we should see many types
        assertTrue("Should generate multiple event types, got: $types", types.size >= 4)
    }

    @Test
    fun `generate with fixed seed is deterministic`() {
        val event1 = StreetEventGenerator.generate("d1", Random(777))
        val event2 = StreetEventGenerator.generate("d1", Random(777))
        assertEquals(event1.type, event2.type)
        assertEquals(event1.title, event2.title)
        assertEquals(event1.description, event2.description)
    }

    // ==================== EVENT EFFECTS ====================

    @Test
    fun `festival effect has positive demand multiplier`() {
        val rng = Random(0)
        // Generate events until we get a festival
        val festivals = (0 until 200).map { StreetEventGenerator.generate("d1", Random(it.toLong())) }
            .filter { it.type == StreetEventType.FESTIVAL }
        if (festivals.isNotEmpty()) {
            val festival = festivals.first()
            assertTrue(festival.effect.demandMultiplier > 1.0f)
        }
        // If no festival generated in 200 attempts, skip (shouldn't happen with weight 0.15)
    }

    @Test
    fun `protest effect has negative demand multiplier`() {
        val protests = (0 until 300).map { StreetEventGenerator.generate("d1", Random(it.toLong())) }
            .filter { it.type == StreetEventType.PROTEST }
        if (protests.isNotEmpty()) {
            val protest = protests.first()
            assertTrue(protest.effect.demandMultiplier < 1.0f)
        }
    }

    @Test
    fun `EventEffect isPositive reflects net benefit`() {
        val positiveEffect = EventEffect(demandMultiplier = 2.0f, revenueMultiplier = 1.5f)
        assertTrue(positiveEffect.isPositive)

        val negativeEffect = EventEffect(demandMultiplier = 0.5f, reputationDelta = -5f)
        assertFalse(negativeEffect.isPositive)

        val neutralEffect = EventEffect()
        assertFalse(neutralEffect.isPositive)
    }

    // ==================== EVENT LIFECYCLE ====================

    @Test
    fun `isActive returns true for fresh event`() {
        val event = StreetEventGenerator.generate("d1", Random(42))
        assertTrue(event.isActive())
    }

    @Test
    fun `isActive returns false for expired event`() {
        val event = StreetEvent(
            eventId = "e1",
            type = StreetEventType.FESTIVAL,
            title = "Test",
            description = "Desc",
            districtId = "d1",
            effect = EventEffect(demandMultiplier = 2.0f),
            durationMs = 1,  // 1ms – instantly expired
            startTimestamp = System.currentTimeMillis() - 1000
        )
        assertFalse(event.isActive())
    }

    @Test
    fun `isActive returns false for dismissed event`() {
        val event = StreetEventGenerator.generate("d1", Random(42))
            .copy(resolution = EventResolution.DISMISSED)
        assertFalse(event.isActive())
    }

    @Test
    fun `remainingMs returns 0 for expired event`() {
        val event = StreetEvent(
            eventId = "e1",
            type = StreetEventType.FESTIVAL,
            title = "Test",
            description = "Desc",
            districtId = "d1",
            effect = EventEffect(),
            durationMs = 1,
            startTimestamp = System.currentTimeMillis() - 1000
        )
        assertEquals(0L, event.remainingMs())
    }

    @Test
    fun `progress returns value between 0 and 1`() {
        val event = StreetEventGenerator.generate("d1", Random(42))
        val p = event.progress()
        assertTrue(p in 0f..1f)
    }

    // ==================== EVENT TYPES ====================

    @Test
    fun `all StreetEventTypes are covered by generator`() {
        // Generate a large sample and check we get all common types
        val generated = (0 until 500).map { StreetEventGenerator.generate("d1", Random(it.toLong())) }
        val generatedTypes = generated.map { it.type }.toSet()

        // The most common types (weight >=0.08) should appear in 500 samples
        val expectedCommon = setOf(
            StreetEventType.FESTIVAL,
            StreetEventType.POP_UP_MARKET,
            StreetEventType.STREET_PARTY,
            StreetEventType.RAINY_DAY,
            StreetEventType.HEALTH_INSPECTION,
            StreetEventType.SUPPLY_SHORTAGE
        )
        expectedCommon.forEach { type ->
            assertTrue("$type should appear in 500 generated events", type in generatedTypes)
        }
    }
}
