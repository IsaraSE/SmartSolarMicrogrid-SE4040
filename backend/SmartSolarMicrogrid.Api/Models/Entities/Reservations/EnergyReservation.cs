using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;
using SmartSolarMicrogrid.Api.Models.Enums;
using System;

namespace SmartSolarMicrogrid.Api.Models.Entities.Reservations;

[BsonIgnoreExtraElements]
public class EnergyReservation
{
    [BsonId]
    [BsonRepresentation(BsonType.ObjectId)]
    public string? ReservationId { get; set; }
    
    public string ReservationNumber { get; set; } = null!;
    
    public string ProsumerNic { get; set; } = null!;
    
    [BsonRepresentation(BsonType.ObjectId)]
    public string StationId { get; set; } = null!;
    
    [BsonRepresentation(BsonType.ObjectId)]
    public string SlotId { get; set; } = null!;
    
    public DateTime ScheduledStartDateTime { get; set; }
    public DateTime ScheduledEndDateTime { get; set; }
    
    [BsonRepresentation(BsonType.String)]
    public ReservationStatus Status { get; set; }
    
    public string QrReference { get; set; } = null!;
    
    public DateTime CreatedAt { get; set; }
    public DateTime UpdatedAt { get; set; }
    public DateTime? CompletedAt { get; set; }
}
