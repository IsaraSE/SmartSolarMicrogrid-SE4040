/*
 * File Name: IUserDetailsRepository.cs
 * Project: Smart Solar Microgrid Trading System
 * Module: SE4040 Enterprise Application Development
 * Author: IT22194862
 * Description: Implementation of IUserDetailsRepository.cs
 * Date: 2026-09-24
 */

using SmartSolarMicrogrid.Api.Models.Entities;

namespace SmartSolarMicrogrid.Api.Repositories.Users;

public interface IUserDetailsRepository : IBaseRepository<UserDetail>
{
    Task<UserDetail?> GetByEmailAsync(string email);
    Task<UserDetail?> GetByNicAsync(string nic);
    Task<UserDetail?> GetByPhoneAsync(string phone);
}
