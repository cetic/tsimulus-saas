package be.cetic.backend.tsimulus

import akka.actor._
import akka.routing.RoundRobinPool
import be.cetic.tsimulus.Utils
import be.cetic.tsimulus.config.Configuration
import org.joda.time.format.{DateTimeFormat, DateTimeFormatterBuilder}
import spray.json._
import api.TsimulusService._
import scala.concurrent.duration._
import scala.annotation.tailrec
import scala.concurrent.Future
import com.typesafe.config.ConfigFactory
import akka.pattern.pipe
import akka.cluster.Cluster
import akka.routing.RoundRobinPool
import com.github.nscala_time.time.Imports._
import org.joda.time.DateTimeConstants

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.apache.kafka.clients.consumer.{ConsumerConfig, KafkaConsumer}

import java.util.Properties
/**
 * Converting into timeseries
 */
class Service extends Actor with ActorLogging {

	//org.apache.log4j.PropertyConfigurator.configure(System.getProperty("user.dir")+"/src/main/resources/log4j.properties")

	val dtf = DateTimeFormat.forPattern("YYYY-MM-dd HH:mm:ss.SSS")

	val datetimeFormatter = {
			val parsers = Array(
					DateTimeFormat.forPattern("YYYY-MM-dd'T'HH:mm:ss.SSS").getParser,
					DateTimeFormat.forPattern("YYYY-MM-dd'T'HH:mm:ss").getParser
					)

					new DateTimeFormatterBuilder().append(null, parsers).toFormatter()
	}

	def receive = {
			//case Generate(config: String) => Future(generateTimeSeries(config)) map  { Result } pipeTo sender() 

	case Generate(config: String) => sender() ! Result(generateTSeries(config))

			// Future((generate(series) foreach (e => println(dtf.print(e._1) + ";" + e._2 + ";" + e._3)))) map { Result } pipeTo sender()
			// Future(generate(series)) map { Result } pipeTo sender() 

	}

	def generateTSeries(config: String): String = {

			val dtf = DateTimeFormat.forPattern("YYYY-MM-dd HH:mm:ss.SSS")

					val datetimeFormatter = {
							val parsers = Array(
									DateTimeFormat.forPattern("YYYY-MM-dd'T'HH:mm:ss.SSS").getParser,
									DateTimeFormat.forPattern("YYYY-MM-dd'T'HH:mm:ss").getParser
									)

									new DateTimeFormatterBuilder().append(null, parsers).toFormatter()
			}

			val series = Utils.config2Results(Configuration(config.parseJson))

					val topic = "test"
					//val zookeeper = ConfigFactory.load().getString("zookeeper.connect")
					val propsProducer = new Properties()
					propsProducer.put("bootstrap.servers", ConfigFactory.load().getString("kafka.brokers"))
					//propsProducer.put("zookeeper.connect", zookeeper)
					//propsProducer.put("client.id", "TSimulusProducer")
					//propsProducer.put("group.id", "test")
					propsProducer.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
					propsProducer.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")

					val producer = new KafkaProducer[String, String](propsProducer)


					//partition_id = hash(partition_key) % number_of_partition


					producer.send(new ProducerRecord[String, String](topic, "date;series;value"))

					generate(series) foreach (e => 
					producer.send(new ProducerRecord[String, String](topic, dtf.print(e._1) + ";" + e._2 + ";" + e._3))
							)

					producer.close()

					/*val propsConsumer = new Properties()
					propsConsumer.put("bootstrap.servers", brokers)
					propsConsumer.put("zookeeper.connect", zookeeper)
					propsConsumer.put("client.id", "TSimulusConsumer")
					propsConsumer.put("group.id", "test")
					propsConsumer.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
					propsConsumer.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
					propsConsumer.put("partition.assignment.strategy", "range");
			    propsConsumer.put("enable.auto.commit", "true");
          propsConsumer.put("auto.commit.interval.ms", "1000");
          propsConsumer.put("session.timeout.ms", "30000");

					val consumer = new KafkaConsumer[String, String](propsConsumer)

					consumer.subscribe(topic)

					while(true){
						val records=consumer.poll(100)
								for (record<-records.asScala){
									println(record)
								}
					}*/

					return ("Sent to the Kafka Message Queue")
	}

	def generate(series: Map[String, Stream[(LocalDateTime, Any)]]): Stream[(LocalDateTime, String, Any)] = {

			val cleanedMap = series.filterNot(_._2.isEmpty)

					if (cleanedMap.isEmpty) Stream.Empty
					else
					{
						val selected = cleanedMap.minBy(e => e._2.head._1)

								val head = selected._2.head
								val tail = selected._2.tail

								val next = series.updated(selected._1, tail)

								(head._1, selected._1, head._2) #:: generate(next)
					}
	}

	/*def generateTimeSeries(content: String): String = {


			val config = Configuration(content.parseJson)

					//val results = Utils.generate(Utils.config2Results(config))

					//  return Source(results.map(x => dtf.print(x._1) + ";" + x._2 + ";" + x._3))
					//(Utils.generate(Utils.config2Results(config)) foreach (e => println(dtf.print(e._1) + ";" + e._2 + ";" + e._3))).toString()

					//val answer = (Utils.generate(Utils.config2Results(config)) foreach (e => dtf.print(e._1) + ";" + e._2 + ";" + e._3)).toString()

					println("testTSimulusOneDate")

					val reference = datetimeFormatter.parseLocalDateTime("2016-01-01T23:56:00.000")
					val last = scala.collection.mutable.Map[String, (LocalDateTime, String)]()
					val results = Utils.eval(config, reference)

					val test = results.map( entry => println(dtf.print(reference) + ";" + entry._1 + ";" + entry._2.getOrElse("NA"))).toString()

					println("testTSimulusMultiDates")

					val series = Utils.config2Results(config)

					println(config.series)
					println(config.toJson)

					println("testTSimulusSeries")

					println(config.series.length)
					println(config.series.head.frequency)


					println(config.from)
					println(config.to)

					//Utils.sampling(config.from, config.to, config.)

					return test.toString()
	}*/
}

/**
 * Bootup the tsimulus service and the associated worker actors
 */
object TsimulusBackend {

	def startOn(system: ActorSystem) {
		system.actorOf(Props[Service].withRouter(RoundRobinPool(100)), name = "tsimulusBackend")
		()
	}
}