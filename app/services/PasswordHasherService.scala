package services

/**
 * Abstract interface for password hashing
 */
trait PasswordHasherService {

  /**
   * Hash the given password
   *
   * @param password The password to hash
   * @return The hashed password
   */
  def hash(password: String): String

  /**
   * Check if the candidate password matches the hashed password
   *
   * @param candidate The password to check for match
   * @param hashed The correct hashed password
   * @return True if passwords match, false otherwise
   */
  def check(candidate: String, hashed: String): Boolean
}
