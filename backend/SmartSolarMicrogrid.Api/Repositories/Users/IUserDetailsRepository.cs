/*
 * File Name: IUserDetailsRepository.cs
 * Project: Smart Solar Microgrid Trading System
 * Module: SE4040 Enterprise Application Development
 * Author: Isara
 * Description: Repository interface for UserDetails collection.
 * Date: 2026-09-14
 */

using SmartSolarMicrogrid.Api.Models.Entities;

namespace SmartSolarMicrogrid.Api.Repositories.Users;

public interface IUserDetailsRepository : IBaseRepository<UserDetail>
{
    Task<UserDetail?> GetByEmailAsync(string email);
    Task<UserDetail?> GetByNicAsync(string nic);
}
