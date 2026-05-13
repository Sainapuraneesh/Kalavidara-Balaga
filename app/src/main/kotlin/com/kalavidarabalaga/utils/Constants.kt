package com.kalavidarabalaga.utils

object Constants {
    const val BASE_URL = "http://10.0.2.2:3000/api/"

    const val TROUPES_COLLECTION = "troupes"
    const val USERS_COLLECTION = "users"
    const val BOOKINGS_COLLECTION = "bookings"

    const val ROLE_USER = "user"
    const val ROLE_TROUPE_LEADER = "troupe_leader"
    const val ROLE_ADMIN = "admin"

    const val EXTRA_TROUPE_ID = "troupe_id"
    const val EXTRA_TROUPE_NAME = "troupe_name"
    const val EXTRA_USER_ROLE = "user_role"

    val KARNATAKA_DISTRICTS = listOf(
        "All Districts",
        "Bagalkot", "Ballari", "Belagavi", "Bengaluru Rural", "Bengaluru Urban",
        "Bidar", "Chamarajanagara", "Chikkaballapur", "Chikkamagaluru", "Chitradurga",
        "Dakshina Kannada", "Davanagere", "Dharwad", "Gadag", "Hassan", "Haveri",
        "Kalaburagi", "Kodagu", "Kolar", "Koppal", "Mandya", "Mysuru", "Raichur",
        "Ramanagara", "Shivamogga", "Tumakuru", "Udupi", "Uttara Kannada",
        "Vijayapura", "Yadgir"
    )

    val FOLK_ART_FORMS = listOf(
        "All Art Forms",
        "Dollu Kunitha",
        "Pooja Kunitha",
        "Goravara Kunitha",
        "Veeragase",
        "Kamsale",
        "Nandi Dhwaja Kunitha",
        "Suggi Kunitha",
        "Kolata",
        "Puja Kunitha",
        "Other"
    )
}
