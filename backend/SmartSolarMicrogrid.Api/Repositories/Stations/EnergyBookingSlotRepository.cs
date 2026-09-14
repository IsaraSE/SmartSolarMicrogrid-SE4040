/*
 * File Name: EnergyBookingSlotRepository.cs
 * Project: Smart Solar Microgrid Trading System
 * Module: SE4040 Enterprise Application Development
 * Author: Isara
 * Description: Repository implementation for EnergyBookingSlots collection with index setup.
 * Date: 2026-09-14
 */

using MongoDB.Driver;
using SmartSolarMicrogrid.Api.Models;
using SmartSolarMicrogrid.Api.Models.Entities;

namespace SmartSolarMicrogrid.Api.Repositories.Stations;

public class EnergyBookingSlotRepository : BaseRepository<EnergyBookingSlot>, IEnergyBookingSlotRepository
{
    public EnergyBookingSlotRepository(IMongoClient mongoClient, Microsoft.Extensions.Options.IOptions<SmartSolarMicrogridDatabaseSettings> settings)
        : base(mongoClient, settings, "EnergyBookingSlots")
    {
        CreateIndexes();
    }

    private void CreateIndexes()
    {
        var stationIdIndex = new CreateIndexModel<EnergyBookingSlot>(
            Builders<EnergyBookingSlot>.IndexKeys.Ascending(s => s.StationId)
        );

        _collection.Indexes.CreateOne(stationIdIndex);
    }

    public async Task<IEnumerable<EnergyBookingSlot>> GetByStationIdAsync(string stationId)
    {
        return await _collection.Find(s => s.StationId == stationId).ToListAsync();
    }
}
