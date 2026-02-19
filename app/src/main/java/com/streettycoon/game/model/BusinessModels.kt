package com.streettycoon.game.model

import com.google.gson.annotations.SerializedName

/**
 * Business Models
 *
 * Expanded business system with all required types, grid-based plots,
 * and supply chain mechanics as specified in the Street Tycoon design.
 */

// ==================== BUSINESS TYPES ====================

/**
 * All business types available in the game.
 * Each type has unique mechanics and customer profiles.
 */
enum class BusinessType {
    // Food & Beverage
    @SerializedName("FOOD_STALL") FOOD_STALL,
    @SerializedName("TEA_SHOP") TEA_SHOP,
    @SerializedName("JUICE_BAR") JUICE_BAR,
    @SerializedName("STREET_FOOD") STREET_FOOD,

    // Retail
    @SerializedName("BOUTIQUE") BOUTIQUE,
    @SerializedName("ELECTRONICS_SHOP") ELECTRONICS_SHOP,
    @SerializedName("SOUVENIR_STALL") SOUVENIR_STALL,

    // Entertainment
    @SerializedName("ARCADE") ARCADE,
    @SerializedName("MUSIC_VENUE") MUSIC_VENUE,
    @SerializedName("CLUB") CLUB,

    // Services
    @SerializedName("REPAIR_SHOP") REPAIR_SHOP,
    @SerializedName("BARBERSHOP") BARBERSHOP,
    @SerializedName("LAUNDRY") LAUNDRY
}

/**
 * Customer segment targeted by each business type.
 */
enum class CustomerSegment {
    @SerializedName("COMMUTER") COMMUTER,
    @SerializedName("TOURIST") TOURIST,
    @SerializedName("LOCAL_RESIDENT") LOCAL_RESIDENT,
    @SerializedName("YOUTH") YOUTH,
    @SerializedName("PROFESSIONAL") PROFESSIONAL,
    @SerializedName("FAMILY") FAMILY
}

/**
 * Static configuration per business type.
 */
data class BusinessConfig(
    val type: BusinessType,
    val displayName: String,
    val description: String,
    val baseCost: Double,
    val baseRevenuePerCustomer: Double,
    val footprint: PlotSize,
    val primarySegments: List<CustomerSegment>,
    val supplyCategories: List<SupplyCategory>,
    val maxStaff: Int,
    val reputationWeight: Float = 1.0f
) {
    companion object {
        fun getConfig(type: BusinessType): BusinessConfig = when (type) {
            BusinessType.FOOD_STALL -> BusinessConfig(
                type = BusinessType.FOOD_STALL,
                displayName = "Food Stall",
                description = "Classic street food serving hot meals",
                baseCost = 500.0,
                baseRevenuePerCustomer = 8.0,
                footprint = PlotSize.SMALL,
                primarySegments = listOf(CustomerSegment.COMMUTER, CustomerSegment.LOCAL_RESIDENT),
                supplyCategories = listOf(SupplyCategory.INGREDIENTS, SupplyCategory.PACKAGING),
                maxStaff = 3
            )
            BusinessType.TEA_SHOP -> BusinessConfig(
                type = BusinessType.TEA_SHOP,
                displayName = "Tea Shop",
                description = "Quick-service tea and snacks",
                baseCost = 300.0,
                baseRevenuePerCustomer = 4.0,
                footprint = PlotSize.TINY,
                primarySegments = listOf(CustomerSegment.COMMUTER, CustomerSegment.PROFESSIONAL),
                supplyCategories = listOf(SupplyCategory.INGREDIENTS),
                maxStaff = 2
            )
            BusinessType.JUICE_BAR -> BusinessConfig(
                type = BusinessType.JUICE_BAR,
                displayName = "Juice Bar",
                description = "Fresh fruit juices and smoothies",
                baseCost = 600.0,
                baseRevenuePerCustomer = 10.0,
                footprint = PlotSize.SMALL,
                primarySegments = listOf(CustomerSegment.YOUTH, CustomerSegment.PROFESSIONAL),
                supplyCategories = listOf(SupplyCategory.INGREDIENTS, SupplyCategory.EQUIPMENT),
                maxStaff = 2
            )
            BusinessType.STREET_FOOD -> BusinessConfig(
                type = BusinessType.STREET_FOOD,
                displayName = "Street Food Cart",
                description = "Mobile cart selling specialty snacks",
                baseCost = 400.0,
                baseRevenuePerCustomer = 6.0,
                footprint = PlotSize.TINY,
                primarySegments = listOf(CustomerSegment.TOURIST, CustomerSegment.YOUTH),
                supplyCategories = listOf(SupplyCategory.INGREDIENTS),
                maxStaff = 1
            )
            BusinessType.BOUTIQUE -> BusinessConfig(
                type = BusinessType.BOUTIQUE,
                displayName = "Boutique",
                description = "Trendy clothing and accessories shop",
                baseCost = 2000.0,
                baseRevenuePerCustomer = 35.0,
                footprint = PlotSize.MEDIUM,
                primarySegments = listOf(CustomerSegment.TOURIST, CustomerSegment.YOUTH, CustomerSegment.PROFESSIONAL),
                supplyCategories = listOf(SupplyCategory.INVENTORY, SupplyCategory.DISPLAY),
                maxStaff = 4,
                reputationWeight = 1.5f
            )
            BusinessType.ELECTRONICS_SHOP -> BusinessConfig(
                type = BusinessType.ELECTRONICS_SHOP,
                displayName = "Electronics Shop",
                description = "Mobile accessories and gadgets",
                baseCost = 3000.0,
                baseRevenuePerCustomer = 50.0,
                footprint = PlotSize.MEDIUM,
                primarySegments = listOf(CustomerSegment.PROFESSIONAL, CustomerSegment.YOUTH),
                supplyCategories = listOf(SupplyCategory.INVENTORY),
                maxStaff = 3,
                reputationWeight = 1.3f
            )
            BusinessType.SOUVENIR_STALL -> BusinessConfig(
                type = BusinessType.SOUVENIR_STALL,
                displayName = "Souvenir Stall",
                description = "Local crafts and tourist memorabilia",
                baseCost = 700.0,
                baseRevenuePerCustomer = 15.0,
                footprint = PlotSize.SMALL,
                primarySegments = listOf(CustomerSegment.TOURIST),
                supplyCategories = listOf(SupplyCategory.INVENTORY),
                maxStaff = 2,
                reputationWeight = 1.2f
            )
            BusinessType.ARCADE -> BusinessConfig(
                type = BusinessType.ARCADE,
                displayName = "Arcade",
                description = "Gaming machines and entertainment center",
                baseCost = 5000.0,
                baseRevenuePerCustomer = 20.0,
                footprint = PlotSize.LARGE,
                primarySegments = listOf(CustomerSegment.YOUTH, CustomerSegment.FAMILY),
                supplyCategories = listOf(SupplyCategory.EQUIPMENT, SupplyCategory.MAINTENANCE),
                maxStaff = 5,
                reputationWeight = 2.0f
            )
            BusinessType.MUSIC_VENUE -> BusinessConfig(
                type = BusinessType.MUSIC_VENUE,
                displayName = "Music Venue",
                description = "Live music performances and events",
                baseCost = 8000.0,
                baseRevenuePerCustomer = 30.0,
                footprint = PlotSize.LARGE,
                primarySegments = listOf(CustomerSegment.YOUTH, CustomerSegment.PROFESSIONAL),
                supplyCategories = listOf(SupplyCategory.EQUIPMENT, SupplyCategory.STAFFING),
                maxStaff = 8,
                reputationWeight = 2.5f
            )
            BusinessType.CLUB -> BusinessConfig(
                type = BusinessType.CLUB,
                displayName = "Club",
                description = "Nightclub with music and drinks",
                baseCost = 15000.0,
                baseRevenuePerCustomer = 60.0,
                footprint = PlotSize.EXTRA_LARGE,
                primarySegments = listOf(CustomerSegment.YOUTH, CustomerSegment.PROFESSIONAL),
                supplyCategories = listOf(SupplyCategory.INGREDIENTS, SupplyCategory.EQUIPMENT, SupplyCategory.STAFFING),
                maxStaff = 12,
                reputationWeight = 3.0f
            )
            BusinessType.REPAIR_SHOP -> BusinessConfig(
                type = BusinessType.REPAIR_SHOP,
                displayName = "Repair Shop",
                description = "Mobile phone and electronics repairs",
                baseCost = 1500.0,
                baseRevenuePerCustomer = 40.0,
                footprint = PlotSize.SMALL,
                primarySegments = listOf(CustomerSegment.COMMUTER, CustomerSegment.LOCAL_RESIDENT),
                supplyCategories = listOf(SupplyCategory.PARTS, SupplyCategory.EQUIPMENT),
                maxStaff = 3
            )
            BusinessType.BARBERSHOP -> BusinessConfig(
                type = BusinessType.BARBERSHOP,
                displayName = "Barbershop",
                description = "Haircuts and grooming services",
                baseCost = 1200.0,
                baseRevenuePerCustomer = 25.0,
                footprint = PlotSize.SMALL,
                primarySegments = listOf(CustomerSegment.LOCAL_RESIDENT, CustomerSegment.PROFESSIONAL),
                supplyCategories = listOf(SupplyCategory.SUPPLIES, SupplyCategory.EQUIPMENT),
                maxStaff = 3
            )
            BusinessType.LAUNDRY -> BusinessConfig(
                type = BusinessType.LAUNDRY,
                displayName = "Laundry",
                description = "Self-service and drop-off laundry",
                baseCost = 2000.0,
                baseRevenuePerCustomer = 15.0,
                footprint = PlotSize.MEDIUM,
                primarySegments = listOf(CustomerSegment.LOCAL_RESIDENT, CustomerSegment.COMMUTER),
                supplyCategories = listOf(SupplyCategory.SUPPLIES, SupplyCategory.EQUIPMENT, SupplyCategory.MAINTENANCE),
                maxStaff = 2
            )
        }
    }
}

// ==================== PLOT GRID ====================

/**
 * Sizes for building footprints on the grid.
 * Maps to grid cell counts (width × height).
 */
enum class PlotSize(val width: Int, val height: Int) {
    @SerializedName("TINY") TINY(1, 1),
    @SerializedName("SMALL") SMALL(1, 2),
    @SerializedName("MEDIUM") MEDIUM(2, 2),
    @SerializedName("LARGE") LARGE(2, 3),
    @SerializedName("EXTRA_LARGE") EXTRA_LARGE(3, 3)
}

/**
 * A single cell in the street grid.
 */
data class GridCell(
    @SerializedName("x") val x: Int,
    @SerializedName("y") val y: Int,
    @SerializedName("plotId") val plotId: String? = null,
    @SerializedName("isBlocked") val isBlocked: Boolean = false
)

/**
 * A plot of land that can hold a business.
 */
data class Plot(
    @SerializedName("plotId") val plotId: String,
    @SerializedName("districtId") val districtId: String,
    @SerializedName("originX") val originX: Int,
    @SerializedName("originY") val originY: Int,
    @SerializedName("size") val size: PlotSize,
    @SerializedName("businessId") val businessId: String? = null,
    @SerializedName("purchaseCost") val purchaseCost: Double,
    @SerializedName("isOwned") val isOwned: Boolean = false,
    @SerializedName("isLocked") val isLocked: Boolean = true
) {
    /** Whether this plot currently has a business built on it. */
    val isOccupied: Boolean get() = businessId != null

    /** All grid cells covered by this plot. */
    fun getCells(): List<Pair<Int, Int>> {
        val cells = mutableListOf<Pair<Int, Int>>()
        for (dx in 0 until size.width) {
            for (dy in 0 until size.height) {
                cells.add(originX + dx to originY + dy)
            }
        }
        return cells
    }
}

/**
 * The street grid for a district.
 * Manages plot placement, collision detection, and available space.
 */
data class StreetGrid(
    @SerializedName("districtId") val districtId: String,
    @SerializedName("columns") val columns: Int,
    @SerializedName("rows") val rows: Int,
    @SerializedName("plots") val plots: List<Plot> = emptyList()
) {
    /** Grid cells as a flat map of (x,y) -> cell. */
    private fun buildCellMap(): Map<Pair<Int, Int>, GridCell> {
        val map = mutableMapOf<Pair<Int, Int>, GridCell>()
        for (x in 0 until columns) {
            for (y in 0 until rows) {
                map[x to y] = GridCell(x, y)
            }
        }
        plots.forEach { plot ->
            plot.getCells().forEach { coord ->
                map[coord] = GridCell(coord.first, coord.second, plot.plotId)
            }
        }
        return map
    }

    /** Check whether a plot of given size can be placed at (x, y). */
    fun canPlace(x: Int, y: Int, size: PlotSize): Boolean {
        if (x < 0 || y < 0 || x + size.width > columns || y + size.height > rows) return false
        val usedCells = plots.flatMap { it.getCells() }.toSet()
        for (dx in 0 until size.width) {
            for (dy in 0 until size.height) {
                if ((x + dx to y + dy) in usedCells) return false
            }
        }
        return true
    }

    /** Return all free (x, y) positions where a given size fits. */
    fun freePositions(size: PlotSize): List<Pair<Int, Int>> {
        val positions = mutableListOf<Pair<Int, Int>>()
        for (x in 0..columns - size.width) {
            for (y in 0..rows - size.height) {
                if (canPlace(x, y, size)) positions.add(x to y)
            }
        }
        return positions
    }

    /** Find a plot by ID. */
    fun findPlot(plotId: String): Plot? = plots.find { it.plotId == plotId }

    /** Return owned plots. */
    fun ownedPlots(): List<Plot> = plots.filter { it.isOwned }

    /** Return plots with active businesses. */
    fun activePlots(): List<Plot> = plots.filter { it.isOccupied }
}

// ==================== SUPPLY CHAIN ====================

/**
 * Categories of supplies required by businesses.
 */
enum class SupplyCategory {
    @SerializedName("INGREDIENTS") INGREDIENTS,
    @SerializedName("INVENTORY") INVENTORY,
    @SerializedName("EQUIPMENT") EQUIPMENT,
    @SerializedName("PARTS") PARTS,
    @SerializedName("SUPPLIES") SUPPLIES,
    @SerializedName("PACKAGING") PACKAGING,
    @SerializedName("DISPLAY") DISPLAY,
    @SerializedName("STAFFING") STAFFING,
    @SerializedName("MAINTENANCE") MAINTENANCE
}

/**
 * A supply item in a business's stock.
 */
data class SupplyItem(
    @SerializedName("itemId") val itemId: String,
    @SerializedName("name") val name: String,
    @SerializedName("category") val category: SupplyCategory,
    @SerializedName("currentStock") var currentStock: Double,
    @SerializedName("maxStock") val maxStock: Double,
    @SerializedName("consumptionPerCustomer") val consumptionPerCustomer: Double,
    @SerializedName("restockCost") val restockCost: Double,
    @SerializedName("lastRestockTimestamp") var lastRestockTimestamp: Long = 0
) {
    /** How many customers can be served with current stock. */
    fun customersServable(): Int {
        if (consumptionPerCustomer <= 0) return Int.MAX_VALUE
        return (currentStock / consumptionPerCustomer).toInt()
    }

    /** Stock level as 0.0–1.0. */
    fun stockRatio(): Float = (currentStock / maxStock).toFloat().coerceIn(0f, 1f)

    /** Whether stock is critically low (below 20%). */
    fun isLow(): Boolean = stockRatio() < 0.2f

    /** Consume supply for one customer; returns false if out of stock. */
    fun consume(): Boolean {
        if (currentStock < consumptionPerCustomer) return false
        currentStock -= consumptionPerCustomer
        return true
    }

    /** Restock to full; returns the cost paid. */
    fun restock(): Double {
        val needed = maxStock - currentStock
        if (needed <= 0) return 0.0
        currentStock = maxStock
        lastRestockTimestamp = System.currentTimeMillis()
        return restockCost * (needed / maxStock)
    }
}

/**
 * Supply chain state for a single business.
 */
data class SupplyChain(
    @SerializedName("businessId") val businessId: String,
    @SerializedName("supplies") val supplies: List<SupplyItem> = emptyList()
) {
    /** Whether all critical supplies are stocked. */
    fun isFullyStocked(): Boolean = supplies.none { it.isLow() }

    /** Minimum customers servable before any supply runs out. */
    fun maxCustomersServable(): Int = supplies.minOfOrNull { it.customersServable() } ?: 0

    /** Total restock cost to fill all supplies. */
    fun totalRestockCost(): Double = supplies.sumOf { it.restockCost * (1.0 - it.stockRatio()) }

    /** Consume supplies for one customer visit. */
    fun consumeForCustomer(): Boolean {
        if (supplies.any { it.consumptionPerCustomer > 0 && it.currentStock < it.consumptionPerCustomer }) {
            return false
        }
        supplies.forEach { it.consume() }
        return true
    }
}

// ==================== BUSINESS INSTANCE ====================

/**
 * An active business placed on a plot.
 */
data class Business(
    @SerializedName("businessId") val businessId: String,
    @SerializedName("plotId") val plotId: String,
    @SerializedName("type") val type: BusinessType,
    @SerializedName("name") val name: String,
    @SerializedName("level") var level: Int = 1,
    @SerializedName("reputation") var reputation: Float = 50f,
    @SerializedName("supplyChain") val supplyChain: SupplyChain,
    @SerializedName("staffIds") val staffIds: List<String> = emptyList(),
    @SerializedName("totalCustomersServed") var totalCustomersServed: Long = 0L,
    @SerializedName("totalRevenue") var totalRevenue: Double = 0.0,
    @SerializedName("openTimestamp") val openTimestamp: Long = System.currentTimeMillis()
) {
    private val config: BusinessConfig get() = BusinessConfig.getConfig(type)

    /** Revenue per customer visit, adjusted for level and reputation. */
    fun revenuePerCustomer(): Double {
        val levelMultiplier = 1.0 + (level - 1) * 0.2
        val repMultiplier = 0.5 + (reputation / 100.0) * 1.0
        return config.baseRevenuePerCustomer * levelMultiplier * repMultiplier
    }

    /** Upgrade cost for the next level. */
    fun upgradeCost(): Double = config.baseCost * 2.0 * Math.pow(1.5, (level - 1).toDouble())

    /** Revenue per hour estimate based on average customer rate. */
    fun estimatedHourlyRevenue(customersPerHour: Int): Double = revenuePerCustomer() * customersPerHour

    /** Apply reputation change (positive or negative). */
    fun adjustReputation(delta: Float) {
        reputation = (reputation + delta).coerceIn(0f, 100f)
    }
}
