package de.bonify.reactnativematomo;

import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import org.matomo.sdk.Matomo;
import org.matomo.sdk.Tracker;
import org.matomo.sdk.TrackerBuilder;
import org.matomo.sdk.extra.TrackHelper;
import javax.annotation.Nonnull;
import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;


public class MatomoModule extends ReactContextBaseJavaModule implements LifecycleEventListener {

    private static final String LOGGER_TAG = "MatomoModule";
    private static Map<Integer, String> customDimensions = new HashMap<>();

    public MatomoModule(ReactApplicationContext reactContext) {
        super(reactContext);
        reactContext.addLifecycleEventListener(this);
    }

    private Matomo matomo;
    private Tracker mMatomoTracker;

    @ReactMethod
    public void initTracker(String url, int id, String dimension) {
        TrackerBuilder builder = TrackerBuilder.createDefault(url, id);
        mMatomoTracker = builder.build(Matomo.getInstance(getReactApplicationContext()));
        if (dimension != null) {
            setCustomDimension(1, dimension);
        }
    }

    @ReactMethod
    public void setAppOptOut(Boolean isOptedOut) {
        mMatomoTracker.setOptOut(isOptedOut);
    }

    @ReactMethod
    public void setUserId(String userId) {
        mMatomoTracker.setUserId(userId);
    }

    @ReactMethod
    public void setCustomDimension(@Nonnull int id, @Nullable String value){
        if(value == null || value.length() == 0) {
            customDimensions.remove(id);
            return;
        }
        customDimensions.put(id, value);
    }

    private TrackHelper getTrackHelper(){
        if (mMatomoTracker == null) {
            throw new RuntimeException("Tracker must be initialized before usage");
        }
        TrackHelper trackHelper = TrackHelper.track();
        for(Map.Entry<Integer, String> entry : customDimensions.entrySet()){
            trackHelper = trackHelper.dimension(entry.getKey(), entry.getValue());
        }
        return trackHelper;
    }

    @ReactMethod
    public void trackScreen(@Nonnull String screen, String title) {
        if (mMatomoTracker == null) {
            throw new RuntimeException("Tracker must be initialized before usage");
        }
        getTrackHelper().screen(screen).title(title).with(mMatomoTracker);
    }

    @ReactMethod
    public void trackEvent(@Nonnull String category, @Nonnull String action, ReadableMap values) {
        if (mMatomoTracker == null) {
            throw new RuntimeException("Tracker must be initialized before usage");
        }
        String name = null;
        Float value = null;
        if (values.hasKey("name") && !values.isNull("name")) {
            name = values.getString("name");
        }
        if (values.hasKey("value") && !values.isNull("value")) {
            value = (float)values.getDouble("value");
        }
        getTrackHelper().event(category, action).name(name).value(value).with(mMatomoTracker);
    }

    @ReactMethod
    public void trackGoal(int goalId, ReadableMap values) {
        if (mMatomoTracker == null) {
            throw new RuntimeException("Tracker must be initialized before usage");
        }
        Float revenue = null;
        if (values.hasKey("revenue") && !values.isNull("revenue")) {
            revenue = (float)values.getDouble("revenue");
        }
        getTrackHelper().goal(goalId).revenue(revenue).with(mMatomoTracker);
    }

    @ReactMethod
    public void trackContentImpression(@Nonnull String name, @Nonnull ReadableMap values) {
        if (mMatomoTracker == null) {
            throw new RuntimeException("Tracker must be initialized before usage");
        }
        String piece = null;
        String target = null;
        if (values.hasKey("piece") && !values.isNull("piece")) {
            piece = values.getString("piece");
        }
        if (values.hasKey("target") && !values.isNull("target")) {
            target = values.getString("target");
        }
        getTrackHelper().impression(name).piece(piece).target(target).with(mMatomoTracker);
    }

    @ReactMethod
    public void trackContentInteraction(@Nonnull String name, @Nonnull ReadableMap values) {
        if (mMatomoTracker == null) {
            throw new RuntimeException("Tracker must be initialized before usage");
        }
        String piece = null;
        String target = null;
        String interaction = null;
        if (values.hasKey("piece") && !values.isNull("piece")) {
            piece = values.getString("piece");
        }
        if (values.hasKey("target") && !values.isNull("target")) {
            target = values.getString("target");
        }
        if (values.hasKey("interaction") && !values.isNull("interaction")) {
            interaction = values.getString("interaction");
        }
        getTrackHelper().interaction(name, interaction).piece(piece).target(target).with(mMatomoTracker);
    }

    @ReactMethod
    public void trackSearch(@Nonnull String query, @Nonnull ReadableMap values) {
        if (mMatomoTracker == null) {
            throw new RuntimeException("Tracker must be initialized before usage");
        }
        String category = null;
        int resultCount = 0;
        if (values.hasKey("category") && !values.isNull("category")) {
            category = values.getString("category");
        }
        if (values.hasKey("resultCount") && !values.isNull("resultCount")) {
            resultCount = values.getInt("resultCount");
        }
        getTrackHelper().search(query).category(category).count(resultCount).with(mMatomoTracker);
    }

    @ReactMethod
    public void trackAppDownload() {
        if (mMatomoTracker == null) {
            throw new RuntimeException("Tracker must be initialized before usage");
        }
        getTrackHelper().track().download().with(mMatomoTracker);
    }

    @Override
    public String getName() {
        return "Matomo";
    }

    @Override
    public void onHostResume() {}

    @Override
    public void onHostPause() {
        if (mMatomoTracker != null) {
            mMatomoTracker.dispatch();
        }
    }

    @Override
    public void onHostDestroy() {}

}