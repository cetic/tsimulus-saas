package be.cetic.tsaas.utils.templates


import freemarker.template.Configuration

object FreeMarkerConfiguration {
  val cfg = new Configuration(Configuration.VERSION_2_3_28)
  cfg.setDefaultEncoding("UTF-8")
}
