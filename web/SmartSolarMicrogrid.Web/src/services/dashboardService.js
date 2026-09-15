/*
 * File Name: dashboardService.js
 * Project: Smart Solar Microgrid Trading System
 * Module: SE4040 Enterprise Application Development
 * Author: Isara
 * Description: API service for fetching dashboard metrics and data.
 * Date: 2026-09-14
 */

import api from './api';

const dashboardService = {
  // Fetch all stations
  getStations: async () => {
    try {
      const response = await api.get('/Stations');
      return response.data;
    } catch (error) {
      console.error('Error fetching stations:', error);
      return { success: false, data: [] };
    }
  },

  // Fetch all users
  getUsers: async () => {
    try {
      const response = await api.get('/Users');
      return response.data;
    } catch (error) {
      console.error('Error fetching users:', error);
      return { success: false, data: [] };
    }
  },

  // Fetch all prosumers (could also just use users and filter)
  getProsumers: async () => {
    try {
      const response = await api.get('/Prosumers');
      return response.data;
    } catch (error) {
      console.error('Error fetching prosumers:', error);
      return { success: false, data: [] };
    }
  },

  // Fetch pending prosumer activations
  getPendingActivations: async () => {
    try {
      const response = await api.get('/Prosumers/pending');
      return response.data;
    } catch (error) {
      console.error('Error fetching pending activations:', error);
      return { success: false, data: [] };
    }
  },

  // Fetch all reservations
  getReservations: async () => {
    try {
      const response = await api.get('/Reservations');
      return response.data;
    } catch (error) {
      console.error('Error fetching reservations:', error);
      return { success: false, data: [] };
    }
  }
};

export default dashboardService;
