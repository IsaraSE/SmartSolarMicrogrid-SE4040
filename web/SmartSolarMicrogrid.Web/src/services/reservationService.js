import api from './api';

export const reservationService = {
  // Get reservations with optional filters
  getReservations: async (nic = '', stationId = '', status = '', date = '') => {
    try {
      const params = new URLSearchParams();
      if (nic) params.append('nic', nic);
      if (stationId) params.append('stationId', stationId);
      if (status && status !== 'All Statuses') params.append('status', status);
      if (date) params.append('date', date);

      const response = await api.get(`/Reservations?${params.toString()}`);
      return response.data;
    } catch (error) {
      console.error('Error fetching reservations:', error);
      throw error;
    }
  },

  // Get reservation by ID
  getReservationById: async (id) => {
    try {
      const response = await api.get(`/Reservations/${id}`);
      return response.data;
    } catch (error) {
      console.error('Error fetching reservation details:', error);
      throw error;
    }
  },

  // Create a new reservation
  createReservation: async (reservationData) => {
    try {
      const response = await api.post('/Reservations', reservationData);
      return response.data;
    } catch (error) {
      console.error('Error creating reservation:', error);
      throw error;
    }
  },

  // Update a reservation status
  updateReservationStatus: async (id, statusData) => {
    try {
      const response = await api.put(`/Reservations/${id}/status`, statusData);
      return response.data;
    } catch (error) {
      console.error('Error updating reservation status:', error);
      throw error;
    }
  },

  // Cancel a reservation
  cancelReservation: async (id) => {
    try {
      const response = await api.put(`/Reservations/${id}/cancel`, {});
      return response.data;
    } catch (error) {
      console.error('Error cancelling reservation:', error);
      throw error;
    }
  },

  // Delete a reservation
  deleteReservation: async (id) => {
    try {
      const response = await api.delete(`/Reservations/${id}`);
      return response.data;
    } catch (error) {
      console.error('Error deleting reservation:', error);
      throw error;
    }
  }
};
