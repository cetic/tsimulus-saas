package api

object TsimulusService {

  case class Generate(config: String)
  case class Result(result: String)
  case class JobFailed(reason: String)
}