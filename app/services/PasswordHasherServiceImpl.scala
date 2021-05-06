package services

import javax.inject.Singleton
import org.mindrot.jbcrypt.BCrypt
import services.PasswordHasherService

@Singleton
class PasswordHasherServiceImpl extends PasswordHasherService {
  def hash(password: String): String = BCrypt.hashpw(password, BCrypt.gensalt())
  def check(candidate: String, hashed: String): Boolean = BCrypt.checkpw(candidate, hashed)
}
