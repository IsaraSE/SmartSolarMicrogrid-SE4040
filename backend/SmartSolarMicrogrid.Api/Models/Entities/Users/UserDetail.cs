using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;
using SmartSolarMicrogrid.Api.Models.Enums;
using System;

namespace SmartSolarMicrogrid.Api.Models.Entities.Users;

public class UserDetail
{
    [BsonId]
    [BsonRepresentation(BsonType.ObjectId)]
    public string? UserId { get; set; }
    
    [BsonIgnoreIfNull]
    public string? Nic { get; set; }
    
    public string FullName { get; set; } = null!;
    public string Email { get; set; } = null!;
    public string Phone { get; set; } = null!;
    public string PasswordHash { get; set; } = null!;
    
    [BsonRepresentation(BsonType.String)]
    public UserRole Role { get; set; }
    
    [BsonRepresentation(BsonType.String)]
    public AccountStatus AccountStatus { get; set; }
    
    public string Address { get; set; } = null!;
    
    [BsonIgnoreIfNull]
    public string? AdditionalInfo { get; set; }
    
    public DateTime CreatedAt { get; set; }
}
