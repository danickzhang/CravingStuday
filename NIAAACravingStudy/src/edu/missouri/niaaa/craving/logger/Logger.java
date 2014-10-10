package edu.missouri.niaaa.craving.logger;

import java.io.Serializable;

import android.util.Log;

@SuppressWarnings("serial")
public class Logger implements Serializable {

	String tagSuffix;

    private Logger(final String tagSuffix) {
        if (null == tagSuffix) {
            throw new IllegalArgumentException("The tag suffix cannot be null");
		} else if (tagSuffix.length() > 23) {
			this.tagSuffix = tagSuffix.substring(0, 22);
		} else {
			this.tagSuffix = tagSuffix;
		}
    }

    public static Logger getLogger(final String tagSuffix) {
        return new Logger(tagSuffix);
    }

    public static Logger getLogger(final Class<?> clazz) {
        return new Logger(clazz.getSimpleName());
    }

	public void d(String msg) {
		Log.d(tagSuffix, msg);
    }

	public void e(String msg) {
		Log.e(tagSuffix, msg);
	}

	public void i(String msg) {
		Log.i(tagSuffix, msg);
	}

	public void v(String msg) {
		Log.v(tagSuffix, msg);
	}

	public void w(String msg) {
		Log.w(tagSuffix, msg);
	}
}

