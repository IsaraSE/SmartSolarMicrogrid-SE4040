/*
 * File Name: UserDetailsRepository.cs
 * Project: Smart Solar Microgrid Trading System
 * Module: SE4040 Enterprise Application Development
 * Author: Isara
 * Description: Repository implementation for UserDetails collection with index setup.
 * Date: 2026-09-14
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

        _collection.Indexes.CreateMany(new[] { emailIndex, nicIndex });
    }

    public async Task<UserDetail?> GetByEmailAsync(string email)
    {
        return await _collection.Find(u => u.Email == email).FirstOrDefaultAsync();
    }

    public async Task<UserDetail?> GetByNicAsync(string nic)
    {
        return await _collection.Find(u => u.Nic == nic).FirstOrDefaultAsync();
    }
}
