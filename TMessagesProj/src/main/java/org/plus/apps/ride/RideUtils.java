package org.plus.apps.ride;

import android.graphics.drawable.Drawable;
import android.location.Location;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.CombinedDrawable;

import java.util.Random;

public class RideUtils {



    public static CombinedDrawable createMenuDrawable(Drawable res){
        CombinedDrawable menuButton = Theme.createCircleDrawableWithIcon(AndroidUtilities.dp(32), res,0);
        menuButton.setIconSize(AndroidUtilities.dp(24), AndroidUtilities.dp(24));
        Theme.setCombinedDrawableColor(menuButton, Theme.getColor(Theme.key_actionBarDefault), false);
        Theme.setCombinedDrawableColor(menuButton, Theme.getColor(Theme.key_dialogTextBlack), true);
        return  menuButton;
    }

    public static CombinedDrawable createMenuDrawable(int res){
        CombinedDrawable menuButton = Theme.createCircleDrawableWithIcon(AndroidUtilities.dp(32), res);
        menuButton.setIconSize(AndroidUtilities.dp(24), AndroidUtilities.dp(24));
        Theme.setCombinedDrawableColor(menuButton, Theme.getColor(Theme.key_actionBarDefault), false);
        Theme.setCombinedDrawableColor(menuButton, Theme.getColor(Theme.key_dialogTextBlack), true);
        return  menuButton;
    }

    protected static Location getLocationInLatLngRad(double radiusInMeters, Location currentLocation) {
        if(currentLocation == null){
            return null;
        }
        double x0 = currentLocation.getLongitude();
        double y0 = currentLocation.getLatitude();

        Random random = new Random();

        // Convert radius from meters to degrees.
        double radiusInDegrees = radiusInMeters / 111320f;

        // Get a random distance and a random angle.
        double u = random.nextDouble();
        double v = random.nextDouble();
        double w = radiusInDegrees * Math.sqrt(u);
        double t = 2 * Math.PI * v;
        // Get the x and y delta values.
        double x = w * Math.cos(t);
        double y = w * Math.sin(t);

        // Compensate the x value.
        double new_x = x / Math.cos(Math.toRadians(y0));

        double foundLatitude;
        double foundLongitude;

        foundLatitude = y0 + y;
        foundLongitude = x0 + new_x;

        Location copy = new Location(currentLocation);
        copy.setLatitude(foundLatitude);
        copy.setLongitude(foundLongitude);
        return copy;
    }




}
