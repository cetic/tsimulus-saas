import scala.util.parsing.json.JSON


JSON.parseRaw("""{"Name":"abc", "age":10}""").get.toString

