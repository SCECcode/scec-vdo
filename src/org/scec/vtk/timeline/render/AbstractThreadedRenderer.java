package org.scec.vtk.timeline.render;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.opensha.commons.util.ExceptionUtils;

import com.google.common.collect.Lists;

/**
 * Abstract implementation of Renderer which does all processing in a separate thread
 * @author kevin
 *
 */
public abstract class AbstractThreadedRenderer implements Renderer {
	
	private ExecutorService exec;
	
	private List<Future<?>> futures;
	
	private RenderStatusListener l;
	
	private int index;
	private int count;
	
	private boolean threadLocalProcessing = false;
	
	// if >0, will block on processFrame when this many are pending (prevents loading too many in memory before flushing)
	private static final int MAX_FRAMES_PENDING = 100;
	
	/* (non-Javadoc)
	 * @see org.scec.vtk.timeline.render.Renderer#init(java.io.File, int, int, int)
	 */
	@Override
	public final void init(File outputFile, int width, int height, double fps, int count) throws IOException {
		if (MAX_FRAMES_PENDING > 0) {
			exec = new ThreadPoolExecutor(1, 1,
                    0L, TimeUnit.MILLISECONDS,
                    new LimitedQueue<Runnable>(MAX_FRAMES_PENDING));
		} else {
			exec = Executors.newSingleThreadExecutor();
		}
		futures = Lists.newArrayList();
		doInit(outputFile, width, height, fps, count);
		index = 0;
		this.count = count;
	}
	
	public static class LimitedQueue<E> extends LinkedBlockingQueue<E>  {
	    public LimitedQueue(int maxSize) {
	        super(maxSize);
	    }

	    @Override
	    public boolean offer(E e)  {
	        // turn offer() and add() into a blocking calls (unless interrupted)
	        try {
	            put(e);
	            return true;
	        } catch(InterruptedException ie) {
	            Thread.currentThread().interrupt();
	        }
	        return false;
	    }

	}
	
	protected abstract void doInit(File outputFile, int width, int height, double fps, int count) throws IOException;
	
	/* (non-Javadoc)
	 * @see org.scec.vtk.timeline.render.Renderer#processFrame(java.awt.image.BufferedImage)
	 */
	@Override
	public final void processFrame(BufferedImage img) throws IOException {
		ProcessRunnable run = new ProcessRunnable(img, index++);
		if (threadLocalProcessing)
			run.run();
		else
			futures.add(exec.submit(run));
	}
	
	private class ProcessRunnable implements Runnable {
		
		private BufferedImage img;
		private int index;

		private ProcessRunnable(BufferedImage img, int index) {
			this.img = img;
			this.index = index;
		}

		@Override
		public void run() {
			try {
				doProcessFrame(img);
				if (l != null)
					l.frameProcessed(index, count);
			} catch (IOException e) {
				ExceptionUtils.throwAsRuntimeException(e);
			}
		}
		
	}
	
	/**
	 * @param threadLocalProcessing if true, processing will be run in the calling thread
	 */
	public void setThreadLocalProcessing(boolean threadLocalProcessing) {
		this.threadLocalProcessing = threadLocalProcessing;
	}
	
	protected abstract void doProcessFrame(BufferedImage img) throws IOException;
	
	/* (non-Javadoc)
	 * @see org.scec.vtk.timeline.render.Renderer#finalize()
	 */
	@Override
	public void finalize() throws IOException {
		// make sure all frames have been processed
		for (Future<?> future : futures) {
			try {
				future.get();
			} catch (Exception e) {
				if (e.getCause() instanceof IOException)
					throw (IOException)e.getCause();
				ExceptionUtils.throwAsRuntimeException(e);
			}
		}
		if (l != null)
			l.finalizeStarted();
		doFinalize();
		if (l != null)
			l.finalizeCompleted();
	}
	
	protected abstract void doFinalize() throws IOException;
	
	public void setRenderStatusListener(RenderStatusListener l) {
		this.l = l;
	}
	
	@Override
	public String toString() {
		return getName();
	}

}
