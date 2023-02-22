# Calendar View
This is a calendar view widget.

## Dependencies
This widget uses [Java Time API](https://docs.oracle.com/javase/8/docs/api/java/time/package-summary.html).\
Step 1. Add in you **build.gradle** file the following code
```
compileOptions {
    // Enable support for the new language APIs
    coreLibraryDesugaringEnabled true
    sourceCompatibility JavaVersion.VERSION_1_8
    targetCompatibility JavaVersion.VERSION_1_8
}
```
Step 2. Add the dependency
```
dependencies {
  //java desugar
  coreLibraryDesugaring 'com.android.tools:desugar_jdk_libs:${version}'
}
```

## Installation
Step 1. Add the JitPack repository to your **build.gradle** file
```
allprojects {
  repositories {
    ...
    maven { url 'https://jitpack.io' }
  }
}
```
Step 2. Add the dependency
```
dependencies {
  implementation 'com.github.akamaccio:CalendarView:${version}'
}
```
## Usage
Add `MmCalendarView` into your layouts or view hierarchy.
```
<net.akamaccio.widget.MmCalendarView
    android:id="@+id/calendar_view"
    android:layout_width="0dp"
    android:layout_height="0dp"
    android:layout_margin="8dp"
    app:layout_constraintStart_toStartOf="parent"
    app:layout_constraintEnd_toEndOf="parent"
    app:layout_constraintTop_toTopOf="parent"
    app:layout_constraintBottom_toBottomOf="parent"
    app:mmcv_arrow_left="@drawable/ic_arrow_left"
    app:mmcv_arrow_right="@drawable/ic_arrow_right" />
```
Set the right and left arrows and, optionally, the other attributes:

* `mmcv_show_other_dates`: show the dates out of the current month (if not set, the outer dates are not shown)
* `mmcv_min_date`: the calendar min date with _yyyyMMdd_ format (if not set, the first day of the current month is set)
* `mmcv_max_date`: the calendar max date with _yyyyMMdd_ format (if not set, the current date set)
* `mmcv_first_day_of_week`: the first day of week (if not set, the default locale value will be set)
* `mmcv_text_month_size`: the month and year text size (if not set, the default color is 24sp)
* `mmcv_text_week_days_size`: the week days text size (if not set, the default color is 16sp)
* `mmcv_text_header_color`: the month, year and week days text color (if not set, the default color is black)
* `mmcv_text_day_size`: the calendar day text size (if not set, the default color is 18sp)
* `mmcv_text_day_color`: the calendar day text color (if not set, the default color is black)

The first 4 attributes can be set programmatically.\
The other parameters that can be optionally set are:

* the year and month formatter
* the days of week formatter
* the calendar day formatter
* the date on click listener
* the date on long click listener
* the month and year click listener
* the month changed listener


## Known Issue
Both widget width and height cannot be set to wrap_content cause of ViewPager2 usage.
