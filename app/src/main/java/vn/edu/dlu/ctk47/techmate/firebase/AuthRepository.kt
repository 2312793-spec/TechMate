package vn.edu.dlu.ctk47.techmate.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import vn.edu.dlu.ctk47.techmate.model.User

object AuthRepository {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    // Bỏ hẳn FirestoreHelper, tự lấy collection trực tiếp
    private fun usersCollection() = db.collection("users")

    fun register(user: User, password: String, onComplete: (Boolean, String?) -> Unit) {
        val email = user.email ?: run {
            onComplete(false, "Email is required")
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = task.result?.user?.uid
                    if (uid != null) {
                        user.id = uid
                        usersCollection().document(uid)
                            .set(user)
                            .addOnSuccessListener {
                                onComplete(true, null)
                            }
                            .addOnFailureListener { e ->
                                // Rollback auth nếu Firestore lỗi
                                task.result?.user?.delete()
                                onComplete(false, e.message ?: "Lưu thông tin thất bại")
                            }
                    } else {
                        onComplete(false, "Không lấy được User ID")
                    }
                } else {
                    onComplete(false, task.exception?.message ?: "Đăng ký thất bại")
                }
            }
    }

    fun login(email: String, password: String, onComplete: (Boolean, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onComplete(true, null)
                } else {
                    onComplete(false, task.exception?.message ?: "Đăng nhập thất bại")
                }
            }
    }

    fun getUserProfile(uid: String, onComplete: (User?) -> Unit) {
        usersCollection().document(uid)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    onComplete(document.toObject(User::class.java))
                } else {
                    onComplete(null)
                }
            }
            .addOnFailureListener {
                onComplete(null)
            }
    }

    fun updateUserProfile(user: User, onComplete: (Boolean, String?) -> Unit) {
        val uid = auth.currentUser?.uid ?: run {
            onComplete(false, "Người dùng chưa đăng nhập")
            return
        }

        user.id = uid
        usersCollection().document(uid)
            .set(user, SetOptions.merge())
            .addOnSuccessListener {
                onComplete(true, null)
            }
            .addOnFailureListener { e ->
                onComplete(false, e.message ?: "Cập nhật thất bại")
            }
    }

    fun logout() {
        auth.signOut()
    }

    fun getCurrentUser(): FirebaseUser? = auth.currentUser
}