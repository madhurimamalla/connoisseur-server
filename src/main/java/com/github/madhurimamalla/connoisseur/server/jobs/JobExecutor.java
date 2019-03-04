package com.github.madhurimamalla.connoisseur.server.jobs;

import com.github.madhurimamalla.connoisseur.server.model.JobHistory;

import com.github.madhurimamalla.connoisseur.server.service.JobService;
import com.github.madhurimamalla.connoisseur.server.service.MovieService;

public final class JobExecutor implements Runnable {

	private JobService jobService;
	private MovieService movieService;
	private RunnableJob currentlyRunningJob;

	public JobExecutor(JobService jobService, MovieService movieService) {
		this.jobService = jobService;
		this.movieService = movieService;
	}

	public RunnableJob getJobInstance(JobHistory job) {
		switch (job.getJobType()) {
		case DEFAULT:
			break;
		case FIREBASE_PUBLISH:
			break;
		case MOVIES_DOWNLOAD:
			return new SyncJob(jobService, job, movieService);
		case SIMILARITY_INFERENCE:
			break;
		default:
			break;

		}
		return null;
	}

	@Override
	public void run() {
		/**
		 * Check if there's any jobs pending/queued in the DB and run that and
		 * wait for it to complete
		 */
		while (true) {
			if(jobService.countOfQueuedJobs() > 0) {
				JobHistory job = jobService.findNextJob();
				if (job != null) {
					currentlyRunningJob = getJobInstance(job);
					if (currentlyRunningJob != null) {
						currentlyRunningJob.run();
						currentlyRunningJob = null;
					}
				}
			}
			try {
				Thread.sleep(20000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
	}
	
	public void cancelJob(JobHistory job) {
		/*
		 * There can be only one running job at a time.
		 */
		if(currentlyRunningJob != null) {
			currentlyRunningJob.cancel();
		}
	}

}
