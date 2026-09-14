/*
 * File Name: IBaseRepository.cs
 * Project: Smart Solar Microgrid Trading System
 * Module: SE4040 Enterprise Application Development
 * Author: Isara
 * Description: Generic base repository interface for standard CRUD operations.
 * Date: 2026-09-14
 */

using System.Linq.Expressions;

namespace SmartSolarMicrogrid.Api.Repositories.Core;

public interface IBaseRepository<T> where T : class
{
    Task<IEnumerable<T>> GetAllAsync();
    Task<IEnumerable<T>> FindAsync(Expression<Func<T, bool>> filter);
    Task<T> GetByIdAsync(string id);
    Task CreateAsync(T entity);
    Task UpdateAsync(string id, T entity);
    Task DeleteAsync(string id);
}
