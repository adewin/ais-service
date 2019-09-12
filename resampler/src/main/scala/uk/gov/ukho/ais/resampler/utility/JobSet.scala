package uk.gov.ukho.ais.resampler.utility

import uk.gov.ukho.ais.resampler.utility.JobSet.Job

object JobSet {
  type Job = () => Unit
}

case class JobSet(jobs: Job*) {

  private val threads: Seq[Thread] = jobs.map { job =>
    val thread = new Thread {
      override def run(): Unit = job()
    }
    thread.setUncaughtExceptionHandler((_, _) => stop())
    thread
  }

  def dispatch(): Unit =
    threads.foreach(_.start())

  def join(): Unit =
    threads.foreach(_.join())

  def stop(): Unit =
    threads.foreach(_.stop())
}
