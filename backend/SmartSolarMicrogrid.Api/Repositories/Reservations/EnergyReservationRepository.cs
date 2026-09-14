/*
 * File Name: EnergyReservationRepository.cs
 * Project: Smart Solar Microgrid Trading System
 * Module: SE4040 Enterprise Application Development
 * Author: Isara
 * Description: Repository implementation for EnergyReservation collection with index setup.
 * Date: 2026-09-14
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
        return await _collection.Find(r => r.ProsumerNic == nic).ToListAsync();
    }

    public async Task<IEnumerable<EnergyReservation>> GetByStationIdAsync(string stationId)
    {
        return await _collection.Find(x => x.StationId == stationId).ToListAsync();
    }

    public async Task<IEnumerable<EnergyReservation>> GetActiveReservationsByStationIdAsync(string stationId)
    {
        return await _collection.Find(x => x.StationId == stationId && 
                                           (x.Status == ReservationStatus.PENDING || 
                                           x.Status == ReservationStatus.APPROVED))
                                .ToListAsync();
    }

    public async Task<EnergyReservation?> GetByQrReferenceAsync(string qrReference)
    {
        return await _collection.Find(r => r.QrReference == qrReference).FirstOrDefaultAsync();
    }
}
