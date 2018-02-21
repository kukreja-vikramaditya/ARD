package com.macbitsgoa.ard.types;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by vikramaditya on 21/2/18.
 */

public class MainActivityType {

    /**
     * Only allow fields to be used instead of pure numbers.
     */
    @IntDef({FORUM, HOME, CHAT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface MAType {
    }

    /**
     * Int value for forum.
     */
    public static final int FORUM = 0;

    /**
     * Int value for home.
     */
    public static final int HOME = 1;

    /**
     * Int value for chat.
     */
    public static final int CHAT = 2;

}
