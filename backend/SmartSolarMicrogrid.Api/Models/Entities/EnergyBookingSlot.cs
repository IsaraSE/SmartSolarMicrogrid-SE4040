using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;
using SmartSolarMicrogrid.Api.Models.Enums;
using System;

namespace SmartSolarMicrogrid.Api.Models.Entities;

public class EnergyBookingSlot
{
    [BsonId]
    [BsonRepresentation(BsonType.ObjectId)]
    public string? SlotId { get; set; }
    
    [BsonRepresentation(BsonType.ObjectId)]
    public string StationId { get; set; } = null!;
    
    public DateTime StartDateTime { get; set; }
    public DateTime EndDateTime { get; set; }
    
    [BsonRepresentation(BsonType.String)]
    public SlotStatus Status { get; set; }
}
