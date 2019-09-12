package uk.gov.ukho.ais.resampler.utility

import uk.gov.ukho.ais.resampler.utility.JobSet.Job

object JobSet {
  type Job = () => Unit
}

case class JobSet(jobs: Seq[Job]) {

  private val threads: Seq[Thread] = jobs.map { job =>
    val thread = new Thread {
      override def run(): Unit = job()
    }
    thread.setUncaughtExceptionHandler((_, _) => interrupt())
    thread
  }

  def dispatch(): Unit =
    threads.foreach(_.start())

  def join(): Unit =
    threads.foreach(_.join())

  def interrupt(): Unit =
    threads.foreach(_.interrupt())
}
