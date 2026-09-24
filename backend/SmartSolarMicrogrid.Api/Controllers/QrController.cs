/*
 * File Name: QrController.cs
 * Project: Smart Solar Microgrid Trading System
 * Module: SE4040 Enterprise Application Development
 * Author: IT22194862
 * Description: Implementation of QrController.cs
 * Date: 2026-09-19
 */

using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using SmartSolarMicrogrid.Api.Models.DTOs;
using SmartSolarMicrogrid.Api.Models.DTOs.Reservations;
using SmartSolarMicrogrid.Api.Services.Reservations;

namespace SmartSolarMicrogrid.Api.Controllers.Reservations;

[ApiController]
[Route("api/qr")]
[Authorize]
public class QrController : ControllerBase
{
    private readonly IReservationService _reservationService;

    public QrController(IReservationService reservationService)
    {
        _reservationService = reservationService;
    }

    [HttpPost("verify")]
    [Authorize(Roles = "GRID_OPERATOR,BACKOFFICE")]
    public async Task<IActionResult> VerifyQr([FromBody] QrVerifyRequestDto request)
    {
        // Performs verification for qr.
        if (string.IsNullOrWhiteSpace(request.QrReference))
        {
            return BadRequest(ApiResponse<QrVerificationResultDto>.ErrorResponse("QR reference is required."));
        }

        var reservation = await _reservationService.GetReservationByQrAsync(request.QrReference);

        if (reservation == null)
        {
            return NotFound(ApiResponse<QrVerificationResultDto>.ErrorResponse("No reservation matches this QR code."));
        }

        if (reservation.Status == Models.Enums.Reservations.ReservationStatus.COMPLETED)
        {
            return BadRequest(ApiResponse<QrVerificationResultDto>.ErrorResponse("This reservation has already been completed."));
        }
        
        if (reservation.Status == Models.Enums.Reservations.ReservationStatus.CANCELLED)
        {
            return BadRequest(ApiResponse<QrVerificationResultDto>.ErrorResponse("This reservation has been cancelled."));
        }

        var result = new QrVerificationResultDto
        {
            Valid = true,
            Message = "Reservation verified successfully.",
            ReservationId = reservation.ReservationId,
            ReservationNumber = reservation.ReservationNumber,
            ProsumerNic = reservation.ProsumerNic,
            ProsumerName = reservation.ProsumerNic, // For now since we don't have user name readily available
            StationId = reservation.StationId,
            StationName = reservation.StationName,
            SlotId = reservation.SlotId,
            BookingDate = reservation.BookingDate,
            StartTime = reservation.StartTime,
            EndTime = reservation.EndTime,
            Status = reservation.Status.ToString()
        };

        return Ok(ApiResponse<QrVerificationResultDto>.SuccessResponse("QR code verified.", result));
    }
}
