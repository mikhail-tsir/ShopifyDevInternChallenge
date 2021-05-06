package services

trait PasswordHasherService {
  def hash(password: String): String
  def check(candidate: String, hashed: String): Boolean
}
