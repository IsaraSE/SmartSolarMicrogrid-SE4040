/*
 * File Name: IEnergyReservationRepository.cs
 * Project: Smart Solar Microgrid Trading System
 * Module: SE4040 Enterprise Application Development
 * Author: IT22194862
 * Description: Implementation of IEnergyReservationRepository.cs
 * Date: 2026-09-19
 */

using SmartSolarMicrogrid.Api.Models.Entities;

namespace SmartSolarMicrogrid.Api.Repositories.Reservations;

public interface IEnergyReservationRepository : IBaseRepository<EnergyReservation>
{
    Task<IEnumerable<EnergyReservation>> GetByProsumerNicAsync(string nic);
    Task<IEnumerable<EnergyReservation>> GetByStationIdAsync(string stationId);
    Task<IEnumerable<EnergyReservation>> GetActiveReservationsByStationIdAsync(string stationId);
    Task<EnergyReservation?> GetByQrReferenceAsync(string qrReference);
}
