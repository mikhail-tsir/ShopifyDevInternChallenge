package services

import javax.inject.Singleton
import org.mindrot.jbcrypt.BCrypt
import services.PasswordHasherService

/**
 * Implementation of the abstract PasswordHasherService trait
 *  using the BCrypt library
 */
@Singleton
class PasswordHasherServiceImpl extends PasswordHasherService {
  // Hashes and salts the given password
  def hash(password: String): String = BCrypt.hashpw(password, BCrypt.gensalt())

  // Checks if the candidate and hashed passwords match
  def check(candidate: String, hashed: String): Boolean = BCrypt.checkpw(candidate, hashed)
}
