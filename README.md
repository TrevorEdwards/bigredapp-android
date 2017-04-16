# Big Red App for Android

## Download?
* Download it from the Play Store [here](https://play.google.com/store/apps/details?id=is.genki.bigredapp.android)!

## What
This is a native android application for Cornell students.  Right now, you can use the app to:
* See what dining halls/cafes are open and what they are serving
* Navigate Cornell's buildings, bus stops, and bike racks
* See what libraries are open
* Discover events around campus
* Lookup people by net ID

### APIs
We currently use the [RedEvents API](https://github.com/TrevorEdwards/RedEvents).

We also previously used the following APIs:
[Kevin Chavez](https://github.com/mrkev)'s  [API](http://redapi-tious.rhcloud.com/).
Cornell Open Data Initiative [API](https://cornelldata.org/).

## Objectives
* 100% free as in beer (no monetization) and free as in speech (open source).
* App: native Android, easy to use, good looking, minimal, clean.
* Code: well-commented, easy to maintain.

## Contributing
* [Pull requests](http://git-scm.com/book/en/v2/GitHub-Contributing-to-a-Project) are very welcome!
* See the [issues page](https://github.com/TrevorEdwards/bigredapp-android/issues).
* Please post any issues for any questions / bug reports / ideas / etc. you have!
* If you are a beginner, please do not hesitate, as I am learning Android dev right now too.

## Overview of the code
* Hopefully you find the comments within the code itself helpful (if not, add an Issue about it!), but for a birds-eye view of how it all works:
* The relevant files are in [`app/src/main`](https://github.com/genkimarshall/bigredapp-android/tree/master/app/src/main).
* Find the activities inside [here](https://github.com/genkimarshall/bigredapp-android/tree/master/app/src/main/java/is/genki/bigredapp/android).
* `MainActivity.java` simply kicks off the `DiningNowFragment.java` fragment, which displays a webview of now.dining.cornell.edu.
* `AboutFragment.java` is a simple fragment to display meta-details about the app.
* All tabs within the navigation drawer are implemented as fragments, and follow a structure similar to that of DiningNowFragment.
* All GET requests are done through `SingletonRequestQueue.java`. It works using a [`RequestQueue`](https://developer.android.com/training/volley/requestqueue.html). The basic idea is that at the very start (in `MainActivity.java`), a `RequestQueue` is started and stays around for the lifetime of the app. So, any time in the app process you can simply access it to add a request on to the queue.

## Up and Running
1. Use Android Studio version >= 1.10
2. Clone this repository
3. Open AS, click "Open an existing Android Studio project"
4. Navigate to where you cloned this repository (it should have a special icon by it in the file browser)
5. Click Ok
6. Under app/src/main/res/values, create a file private_keys.xml and copy and paste the below, filling in your relevant key:
```
<resources>
    <!-- Google Maps API Key -->
    <string name="google_maps_key" translatable="false" templateMergeStrategy="preserve">
        YOUR_KEY_HERE
    </string>
</resources>
```

If you do not have a key, see: https://developers.google.com/maps/documentation/android-api/?hl=en_US 

## OpenSourceCornell Club
* [Website](http://orgsync.rso.cornell.edu/org/opensourcecornell)
* [Facebook Group](https://www.facebook.com/groups/opensourcecornell)
* [Slack chat](https://opensourcecornell.slack.com/signup)
* freenode IRC: #opensourcecornell

