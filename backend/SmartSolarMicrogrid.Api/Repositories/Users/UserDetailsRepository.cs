/*
 * File Name: UserDetailsRepository.cs
 * Project: Smart Solar Microgrid Trading System
 * Module: SE4040 Enterprise Application Development
 * Author: IT22194862
 * Description: Implementation of UserDetailsRepository.cs
 * Date: 2026-09-16
 */

using MongoDB.Driver;
using SmartSolarMicrogrid.Api.Models;
using SmartSolarMicrogrid.Api.Models.Entities;

namespace SmartSolarMicrogrid.Api.Repositories.Users;

public class UserDetailsRepository : BaseRepository<UserDetail>, IUserDetailsRepository
{
    public UserDetailsRepository(IMongoClient mongoClient, Microsoft.Extensions.Options.IOptions<SmartSolarMicrogridDatabaseSettings> settings)
        : base(mongoClient, settings, "UserDetails")
    {
        CreateIndexes();
    }

    private void CreateIndexes()
    {
        // Handles the creation of indexes.
        try
        {
            // Unique index for Email
            var emailIndex = new CreateIndexModel<UserDetail>(
                Builders<UserDetail>.IndexKeys.Ascending(u => u.Email),
                new CreateIndexOptions { Unique = true }
            );

            // Unique sparse/partial index for NIC (only enforces uniqueness for Prosumers where NIC is not null)
            var nicIndex = new CreateIndexModel<UserDetail>(
                Builders<UserDetail>.IndexKeys.Ascending(u => u.Nic),
                new CreateIndexOptions 
                { 
                    Unique = true, 
                    Sparse = true
                }
            );

            // Unique index for Phone
            var phoneIndex = new CreateIndexModel<UserDetail>(
                Builders<UserDetail>.IndexKeys.Ascending(u => u.Phone),
                new CreateIndexOptions { Unique = true, Sparse = true }
            );

            _collection.Indexes.CreateMany(new[] { emailIndex, nicIndex, phoneIndex });
        }
        catch (Exception ex)
        {
            Console.WriteLine($"[Warning] Failed to create MongoDB indexes. This is usually due to existing duplicate data: {ex.Message}");
        }
    }

    public async Task<UserDetail?> GetByEmailAsync(string email)
    {
        // Retrieves by email data from the system.
        return await _collection.Find(u => u.Email == email).FirstOrDefaultAsync();
    }

    public async Task<UserDetail?> GetByNicAsync(string nic)
    {
        // Retrieves by nic data from the system.
        return await _collection.Find(u => u.Nic == nic).FirstOrDefaultAsync();
    }

    public async Task<UserDetail?> GetByPhoneAsync(string phone)
    {
        // Retrieves by phone data from the system.
        return await _collection.Find(u => u.Phone == phone).FirstOrDefaultAsync();
    }
}
