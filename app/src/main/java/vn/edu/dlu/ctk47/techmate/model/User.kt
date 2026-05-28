package vn.edu.dlu.ctk47.techmate.model

import com.google.firebase.firestore.PropertyName

data class User (
    // Sử dụng @get:PropertyName để đảm bảo Mapping chính xác khi dùng Firestore
    @get:PropertyName("id") @set:PropertyName("id")
    var id: String? = null,

    var name: String? = null,
    var email: String? = null,
    var role: String? = null,
    var avatar: String? = null,
    var address: String? = null,
    var phone: String? = null,

    // Bổ sung trường này để đồng bộ với app Admin
    var createdAt: Long = 0
)