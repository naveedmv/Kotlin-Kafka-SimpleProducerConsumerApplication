// https://www.netguru.com/codestories/traversing-through-dates-with-kotlin-range-expressions

import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DateIterator(val startDate: LocalDate,
                   val endDateInclusive: LocalDate,
                   val stepDays: Long): Iterator<LocalDate> {
    private var currentDate = startDate

    override fun hasNext() = currentDate <= endDateInclusive

    override fun next(): LocalDate {

        val next = currentDate

        currentDate = currentDate.plusDays(stepDays)

        return next

    }

}

class DateProgression(override val start: LocalDate,
                      override val endInclusive: LocalDate,
                      val stepDays: Long = 1) :
    Iterable<LocalDate>, ClosedRange<LocalDate> {

    override fun iterator(): Iterator<LocalDate> =
        DateIterator(start, endInclusive, stepDays)

    infix fun step(days: Long) = DateProgression(start, endInclusive, days)

}

operator fun LocalDate.rangeTo(other: LocalDate) = DateProgression(this, other)

/*
fun main(){
    val startDate = LocalDate.of(2020, 1, 1)
    val endDate = LocalDate.of(2020, 1, 10)

    for (date in startDate..endDate step 1) {
        println("${date.year} ${date.format(DateTimeFormatter.ofPattern("MMM"))} ${date.dayOfMonth}")
    }
}*/