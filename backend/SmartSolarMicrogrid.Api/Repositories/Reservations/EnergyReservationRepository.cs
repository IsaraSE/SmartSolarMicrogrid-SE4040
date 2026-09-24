/*
 * File Name: EnergyReservationRepository.cs
 * Project: Smart Solar Microgrid Trading System
 * Module: SE4040 Enterprise Application Development
 * Author: IT22194862
 * Description: Implementation of EnergyReservationRepository.cs
 * Date: 2026-09-16
 */

using MongoDB.Driver;
using SmartSolarMicrogrid.Api.Models;
using SmartSolarMicrogrid.Api.Models.Entities;

namespace SmartSolarMicrogrid.Api.Repositories.Reservations;

public class EnergyReservationRepository : BaseRepository<EnergyReservation>, IEnergyReservationRepository
{
    public EnergyReservationRepository(IMongoClient mongoClient, Microsoft.Extensions.Options.IOptions<SmartSolarMicrogridDatabaseSettings> settings)
        : base(mongoClient, settings, "EnergyReservation")
    {
        CreateIndexes();
    }

    private void CreateIndexes()
    {
        // Handles the creation of indexes.
        var qrIndex = new CreateIndexModel<EnergyReservation>(
            Builders<EnergyReservation>.IndexKeys.Ascending(r => r.QrReference),
            new CreateIndexOptions { Unique = true, Sparse = true }
        );

        var nicIndex = new CreateIndexModel<EnergyReservation>(
            Builders<EnergyReservation>.IndexKeys.Ascending(r => r.ProsumerNic)
        );

        var stationIdIndex = new CreateIndexModel<EnergyReservation>(
            Builders<EnergyReservation>.IndexKeys.Ascending(r => r.StationId)
        );

        _collection.Indexes.CreateMany(new[] { qrIndex, nicIndex, stationIdIndex });
    }

    public async Task<IEnumerable<EnergyReservation>> GetByProsumerNicAsync(string nic)
    {
        // Retrieves by prosumer nic data from the system.
        return await _collection.Find(r => r.ProsumerNic == nic).ToListAsync();
    }

    public async Task<IEnumerable<EnergyReservation>> GetByStationIdAsync(string stationId)
    {
        // Retrieves by station id data from the system.
        return await _collection.Find(x => x.StationId == stationId).ToListAsync();
    }

    public async Task<IEnumerable<EnergyReservation>> GetActiveReservationsByStationIdAsync(string stationId)
    {
        // Retrieves active reservations by station id data from the system.
        return await _collection.Find(x => x.StationId == stationId && 
                                           (x.Status == ReservationStatus.PENDING || 
                                           x.Status == ReservationStatus.APPROVED))
                                .ToListAsync();
    }

    public async Task<EnergyReservation?> GetByQrReferenceAsync(string qrReference)
    {
        // Retrieves by qr reference data from the system.
        return await _collection.Find(r => r.QrReference == qrReference).FirstOrDefaultAsync();
    }
}
