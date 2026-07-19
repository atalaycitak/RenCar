package com.example.rencar_pair.data.repository.location

import com.example.rencar_pair.domain.model.VehiclePoint
import com.example.rencar_pair.domain.repository.RideLocationRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.Random

class FakeRideLocationRepository : RideLocationRepository {
    override fun observeActiveVehicleLocation(): Flow<VehiclePoint> = flow {
        val startLat = 41.03720
        val startLng = 28.98590
        var currentLat = startLat
        var currentLng = startLng
        val random = Random()

        emit(VehiclePoint(currentLat, currentLng))
        delay(1500)

        var tick = 1
        var currentDirection = 0 // 0: Kuzey, 1: Doğu, 2: Güney, 3: Batı
        
        while (true) {
            // %30 ihtimalle yön değiştir (Sert dönüşler - Zikzak)
            if (random.nextDouble() > 0.70) {
                val turn = if (random.nextBoolean()) 1 else -1
                currentDirection = (currentDirection + turn + 4) % 4
            }

            // Seçilen yöne doğru ilerle (saniyede yaklaşık 10-15 metre)
            val stepSize = 0.00015
            when (currentDirection) {
                0 -> currentLat += stepSize
                1 -> currentLng += stepSize
                2 -> currentLat -= stepSize
                3 -> currentLng -= stepSize
            }

            // Çok ufak rastgele "titreme" ekle ki dümdüz robot gibi olmasın
            val noiseLat = (random.nextDouble() - 0.5) * 0.00005
            val noiseLng = (random.nextDouble() - 0.5) * 0.00005
            
            emit(VehiclePoint(currentLat + noiseLat, currentLng + noiseLng))
            tick++
            delay(1000) // Her saniye yeni bir konum
        }
    }
}
