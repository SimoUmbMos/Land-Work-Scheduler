package com.simosc.landworkscheduler

import android.os.Bundle
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.maps.model.LatLng
import com.simosc.landworkscheduler.domain.model.Land
import com.simosc.landworkscheduler.domain.model.Note
import com.simosc.landworkscheduler.domain.model.Work
import com.simosc.landworkscheduler.domain.model.Zone
import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDateTime

@RunWith(AndroidJUnit4::class)
class ModelsParcelTest {

    @Test
    fun test_LandParcel(){
        val originalLand = Land(
            id = 1,
            title = "mock land",
            color = Color.Black,
            border = listOf(
                LatLng(0.0, 0.0),
                LatLng(1.0, 0.0),
                LatLng(1.0, 1.0),
                LatLng(0.0, 1.0),
            ),
            holes = listOf(
                listOf(
                    LatLng(0.25, 0.25),
                    LatLng(0.5, 0.25),
                    LatLng(0.5, 0.5),
                    LatLng(0.25, 0.5),
                ),
                listOf(
                    LatLng(0.5, 0.5),
                    LatLng(0.75, 0.5),
                    LatLng(0.75, 0.75),
                    LatLng(0.5, 0.75),
                ),
            )
        )

        val bundle = Bundle().apply {
            putParcelable("land",originalLand)
        }

        bundle.getParcelable("land",Land::class.java).let{ returnedLand ->

            Log.d("LandParcelTest", "originalLand = $originalLand")
            Log.d("LandParcelTest", "returnedLand = $returnedLand")

            assertEquals(originalLand.id,returnedLand?.id)
            assertEquals(originalLand.title,returnedLand?.title)
            assertEquals(originalLand.color.toArgb(),returnedLand?.color?.toArgb())
            assertEquals(originalLand.border,returnedLand?.border)
            assertEquals(originalLand.holes,returnedLand?.holes)
            assertEquals(originalLand,returnedLand)
        }

    }

    @Test
    fun test_NoteParcel(){
        val originalNote = Note(
            id = 1,
            lid = 1,
            title = "mock Note",
            desc = "a note to test parcel",
            color = Color.Black,
            center = LatLng(0.0, 0.0),
            radius = 15.0,
            created = LocalDateTime.now().minusDays(1),
            edited = LocalDateTime.now(),
        )

        val bundle = Bundle().apply {
            putParcelable("note",originalNote)
        }

        bundle.getParcelable("note",Note::class.java).let{ returnedNote ->

            Log.d("NoteParcelTest", "originalNote = $originalNote")
            Log.d("NoteParcelTest", "returnedNote = $returnedNote")

            assertEquals(originalNote.id,returnedNote?.id)
            assertEquals(originalNote.title,returnedNote?.title)
            assertEquals(originalNote.desc,returnedNote?.desc)
            assertEquals(originalNote.color.toArgb(),returnedNote?.color?.toArgb())
            assertEquals(originalNote.center,returnedNote?.center)
            assertEquals(originalNote.radius,returnedNote?.radius)
            assertEquals(originalNote.created,returnedNote?.created)
            assertEquals(originalNote.edited,returnedNote?.edited)
            assertEquals(originalNote,returnedNote)
        }
    }

    @Test
    fun test_ZoneParcel(){
        val originalZone = Zone(
            id = 1,
            lid = 1,
            title = "mock Zone",
            color = Color.Black,
            border = listOf(
                LatLng(0.0, 0.0),
                LatLng(1.0, 0.0),
                LatLng(1.0, 1.0),
                LatLng(0.0, 1.0),
            ),
            holes = listOf(
                listOf(
                    LatLng(0.25, 0.25),
                    LatLng(0.5, 0.25),
                    LatLng(0.5, 0.5),
                    LatLng(0.25, 0.5),
                ),
                listOf(
                    LatLng(0.5, 0.5),
                    LatLng(0.75, 0.5),
                    LatLng(0.75, 0.75),
                    LatLng(0.5, 0.75),
                ),
            )
        )

        val bundle = Bundle().apply {
            putParcelable("zone",originalZone)
        }

        bundle.getParcelable("zone",Zone::class.java).let{ returnedZone ->

            Log.d("ZoneParcelTest", "originalLand = $originalZone")
            Log.d("ZoneParcelTest", "returnedZone = $returnedZone")

            assertEquals(originalZone.id,returnedZone?.id)
            assertEquals(originalZone.lid,returnedZone?.lid)
            assertEquals(originalZone.title,returnedZone?.title)
            assertEquals(originalZone.color.toArgb(),returnedZone?.color?.toArgb())
            assertEquals(originalZone.border,returnedZone?.border)
            assertEquals(originalZone.holes,returnedZone?.holes)
            assertEquals(originalZone,returnedZone)
        }
    }

    @Test
    fun test_WorkParcel(){
        val work1 = Work(
            id = 1,
            lid = null,
            zid = null,
            title = "mock work 1",
            desc = "an test scheduled work",
            date = LocalDateTime.now().plusDays(1),
            created = LocalDateTime.now().minusDays(1),
            edited = LocalDateTime.now(),
        )
        val work2 = Work(
            id = 2,
            lid = 1,
            zid = null,
            title = "mock work 2",
            desc = "another test for scheduled work",
            date = LocalDateTime.now().plusDays(2),
            created = LocalDateTime.now().minusDays(3),
            edited = LocalDateTime.now().minusDays(2),
        )
        val work3 = Work(
            id = 3,
            lid = 1,
            zid = 2,
            title = "mock work 3",
            desc = "last test for scheduled work",
            date = LocalDateTime.now(),
            created = LocalDateTime.now().minusDays(2),
            edited = LocalDateTime.now().minusDays(1),
        )

        val bundle = Bundle().apply {
            putParcelable("work1",work1)
            putParcelable("work2",work2)
            putParcelable("work3",work3)
        }

        bundle.getParcelable("work1",Work::class.java).let{ returnedWork1 ->

            Log.d("WorkParcelTest", "work1 = $work1")
            Log.d("WorkParcelTest", "returnedWork1 = $returnedWork1")

            assertEquals(work1.id,returnedWork1?.id)
            assertEquals(work1.lid,returnedWork1?.lid)
            assertEquals(work1.zid,returnedWork1?.zid)
            assertEquals(work1.title,returnedWork1?.title)
            assertEquals(work1.desc,returnedWork1?.desc)
            assertEquals(work1.date,returnedWork1?.date)
            assertEquals(work1.created,returnedWork1?.created)
            assertEquals(work1.edited,returnedWork1?.edited)
            assertEquals(work1,returnedWork1)
        }

        bundle.getParcelable("work2",Work::class.java).let{ returnedWork2 ->

            Log.d("WorkParcelTest", "work2 = $work2")
            Log.d("WorkParcelTest", "returnedWork2 = $returnedWork2")

            assertEquals(work2.id,returnedWork2?.id)
            assertEquals(work2.lid,returnedWork2?.lid)
            assertEquals(work2.zid,returnedWork2?.zid)
            assertEquals(work2.title,returnedWork2?.title)
            assertEquals(work2.desc,returnedWork2?.desc)
            assertEquals(work2.date,returnedWork2?.date)
            assertEquals(work2.created,returnedWork2?.created)
            assertEquals(work2.edited,returnedWork2?.edited)
            assertEquals(work2,returnedWork2)
        }

        bundle.getParcelable("work3",Work::class.java).let{ returnedWork3 ->

            Log.d("WorkParcelTest", "work3 = $work3")
            Log.d("WorkParcelTest", "returnedWork3 = $returnedWork3")

            assertEquals(work3.id,returnedWork3?.id)
            assertEquals(work3.lid,returnedWork3?.lid)
            assertEquals(work3.zid,returnedWork3?.zid)
            assertEquals(work3.title,returnedWork3?.title)
            assertEquals(work3.desc,returnedWork3?.desc)
            assertEquals(work3.date,returnedWork3?.date)
            assertEquals(work3.created,returnedWork3?.created)
            assertEquals(work3.edited,returnedWork3?.edited)
            assertEquals(work3,returnedWork3)
        }
    }
}