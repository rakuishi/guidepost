package com.rakuishi.guidepost

import android.location.Location
import kotlin.math.*

object LocationUtils {

    /**
     * https://www.igismap.com/formula-to-find-bearing-or-heading-angle-between-two-points-latitude-longitude/
     *
     * @param lat1 latitude 1
     * @param lon1 longitude 1
     * @param lat2 latitude 2
     * @param lon2 longitude 2
     * @return bearing(0~360)
     */
    fun bearing(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val radLat1 = Math.toRadians(lat1)
        val radLat2 = Math.toRadians(lat2)
        val diffLon = Math.toRadians(lon2 - lon1)
        val x = cos(radLat1) * sin(radLat2) - sin(radLat1) * cos(radLat2) * cos(diffLon)
        val y = cos(radLat2) * sin(diffLon)
        return (Math.toDegrees(atan2(y, x)) + 360) % 360
    }

    /**
     * https://qiita.com/chiyoyo/items/b10bd3864f3ce5c56291
     *
     * @param lat1 latitude 1
     * @param lon1 longitude 1
     * @param lat2 latitude 2
     * @param lon2 longitude 2
     * @return distance(m)
     */
    fun distance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val radLat1 = Math.toRadians(lat1)
        val radLon1 = Math.toRadians(lon1)
        val radLat2 = Math.toRadians(lat2)
        val radLon2 = Math.toRadians(lon2)
        val r = 6378137.0 // equatorial radius
        val averageLat = (radLat1 - radLat2) / 2
        val averageLon = (radLon1 - radLon2) / 2
        return 2 * r * asin(sqrt(sin(averageLat).pow(2) + cos(radLat1) * cos(radLat2) * sin(averageLon).pow(2)))
    }

    fun distance(location1: Location, location2: Location): Double {
        return distance(location1.latitude, location1.longitude, location2.latitude, location2.longitude)
    }
}