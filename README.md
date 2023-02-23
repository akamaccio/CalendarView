# Calendar View
[![](https://jitpack.io/v/akamaccio/CalendarView.svg)](https://jitpack.io/#akamaccio/CalendarView)\
This is a calendar view widget.

## Dependencies
This widget uses [Java Time APIs](https://docs.oracle.com/javase/8/docs/api/java/time/package-summary.html).\
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
```kotlin
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

* `mmcv_show_other_dates`: show the dates out of the current month (by default, the outer dates are not shown)
* `mmcv_min_date`: the calendar min date with _yyyyMMdd_ format (the default value is the first day of the current month)
* `mmcv_max_date`: the calendar max date with _yyyyMMdd_ format (the default value is the current date)
* `mmcv_first_day_of_week`: the first day of week (the default value is the default locale value)
* `mmcv_text_month_size`: the month and year text size (the default color is 24sp)
* `mmcv_text_week_days_size`: the week days text size (the default color is 16sp)
* `mmcv_text_header_color`: the month, year and week days text color (the default color is black)
* `mmcv_text_day_size`: the calendar day text size (the default color is 18sp)
* `mmcv_text_day_color`: the calendar day text color (the default color is black)

The first 4 attributes can be set programmatically.\
The other parameters that can be optionally set are:

* the year and month text formatter
* the days of week text formatter
* the calendar day text formatter
* the date on click listener
* the date on long click listener
* the month and year click listener
* the month changed listener

**To apply the parameters change the `build()` method must be called.**
```kotlin
 mainBinding.calendarView.apply {
    minDate = LocalDate.of(2018, 1, 1)
    maxDate = LocalDate.now()
    showOtherDates = true
    firstDayOfWeek = DayOfWeek.SATURDAY
    build()
}
```

## Relevant methods
```kotlin
/**
* This method scrolls the calendar to the provided [Temporal].
* @param temporal The temporal reference.
* @param smoothly <code>TRUE</code> if the scroll must be smooth, <code>FALSE</code> (default) otherwise.
*/
fun moveTo(temporal : Temporal, smoothly : Boolean = false)

/**
* This method refreshed all calendar.
* This method has a high computing cost and must be used just in case many calendar items has been updated.
* Use [notifyCalendarItemsChanged] instead.
*/
fun notifyCalendarChanged()

/**
* This method updated the provided [Temporal] items (e.g. [LocalDate] and [YearMonth]).
* @param items The list of temporal items to update.
*/
fun <T : Temporal> notifyCalendarItemsChanged(vararg items : T)
    
/**
* This method checks if the provided [LocalDate] is withing the min and max dates.
* @param localDate The local date to check.
* @return TRUE if the provided [LocalDate] is withing the min and max dates, FALSE otherwise.
*/
fun isInRange(localDate: LocalDate) : Boolean
```

## Decorators

The widget can be enriched via [decorators](https://github.com/akamaccio/CalendarView/blob/master/mmcalendarview/src/main/java/net/akamaccio/widget/MmCalendarDayDecorator.kt) which allow to change the calendar day element style and content under defined conditions.

**This feature with click listeners and calendar notification change methods allow to implement the calendar dates selection paradigm.**


## Known Issue
Both widget width and height cannot be set to wrap_content cause of ViewPager2 usage.
