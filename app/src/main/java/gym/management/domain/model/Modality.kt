package gym.management.domain.model

data class Modality(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val schedule: String = "",
    val schedules: List<String> = emptyList(),
    val price: Double = 0.0,
    val frequency: String = "",
    val active: Boolean = true
)
