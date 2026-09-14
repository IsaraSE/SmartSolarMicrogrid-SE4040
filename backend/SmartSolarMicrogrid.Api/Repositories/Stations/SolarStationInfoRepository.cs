/*
 * File Name: SolarStationInfoRepository.cs
 * Project: Smart Solar Microgrid Trading System
 * Module: SE4040 Enterprise Application Development
 * Author: Isara
 * Description: Repository implementation for SolarStationInfo collection.
 * Date: 2026-09-14
 */

using MongoDB.Driver;
using SmartSolarMicrogrid.Api.Models;
using SmartSolarMicrogrid.Api.Models.Entities;

namespace SmartSolarMicrogrid.Api.Repositories.Stations;

public class SolarStationInfoRepository : BaseRepository<SolarStationInfo>, ISolarStationInfoRepository
{
    public SolarStationInfoRepository(IMongoClient mongoClient, Microsoft.Extensions.Options.IOptions<SmartSolarMicrogridDatabaseSettings> settings)
        : base(mongoClient, settings, "SolarStationInfo")
    {
    }
}
