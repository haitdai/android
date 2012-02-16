package com.duckduckgo.mobile.android.download;

import java.lang.ref.WeakReference;

import com.duckduckgo.mobile.android.tasks.DownloadBitmapTask;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

//TODO: Any way we can limit the number of simultaneous downloads? Do we need to?
//TODO: Any way we can find out that multiple objects are attempting to get the same item?
//TODO: Think we need a download service for those that these tasks can register and wait on, or some such...

public class ImageDownloader {
	private static final String TAG = "ImageDownloader";
	
	private final ImageCache cache;
	
	public ImageDownloader(ImageCache cache) {
		this.cache = cache;
	}
	
	//TODO: Should take a Downloadable object
	public void download(String url, DownloadableImage image) {
		Bitmap bitmap = cache.getBitmapFromCache(url);
		
		if (bitmap == null) {
			attemptDownload(url, image);
		} else {
			cancelPreviousDownload(url, image);
			image.setBitmap(bitmap);
		}
	}
	
	//TODO: Should take a Downloadable object
	private void attemptDownload(String url, DownloadableImage image) {
		if (url == null) {
			//No image to download!
			image.setDownloadBitmapTask(null);
			image.setBitmap(null);
			return;
		}
		
		if (!cancelPreviousDownload(url, image)) {
			//We are already downloading that exact image
			return;
		}
		
		DownloadBitmapTask task = new DownloadBitmapTask(image, cache);
		//TODO: Should just add the task to the Downloadable object (Downloadable needs a setTask call)
		image.setDownloadBitmapTask(task);
		image.setDefault();
		task.execute(url);
	}
	
	private static boolean cancelPreviousDownload(String url, DownloadableImage image) {
		DownloadBitmapTask task = image.getDownloadBitmapTask();
		
		if (task != null) {
			String bitmapUrl = task.url;
			if ((bitmapUrl == null) || !bitmapUrl.equals(url)) {
				task.cancel(true);
			} else {
				return false;
			}
		}
		return true;
	}
}
