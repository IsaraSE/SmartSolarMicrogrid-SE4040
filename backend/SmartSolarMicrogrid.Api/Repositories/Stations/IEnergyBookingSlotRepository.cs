/*
 * File Name: IEnergyBookingSlotRepository.cs
 * Project: Smart Solar Microgrid Trading System
 * Module: SE4040 Enterprise Application Development
 * Author: IT22154880
 * Description: Implementation of IEnergyBookingSlotRepository.cs
 * Date: 2026-09-20
 */

using SmartSolarMicrogrid.Api.Models.Entities;

namespace SmartSolarMicrogrid.Api.Repositories.Stations;

public interface IEnergyBookingSlotRepository : IBaseRepository<EnergyBookingSlot>
{
    Task<IEnumerable<EnergyBookingSlot>> GetByStationIdAsync(string stationId);
}
