/*
 * File Name: userService.js
 * Project: Smart Solar Microgrid Trading System
 * Module: SE4040 Enterprise Application Development
 * Author: Isara
 * Description: Service for handling User-related API requests.
 * Date: 2026-09-14
 */

import api from './api';

const userService = {
    /**
     * Fetch all non-prosumer users (Admins, Grid Operators, Backoffice)
     */
    getAllUsers: async () => {
        try {
            const response = await api.get('/Users');
            return response.data;
        } catch (error) {
            console.error('Error fetching users:', error);
            throw error;
        }
    },

    /**
     * Get a specific user by ID
     */
    getUserById: async (userId) => {
        try {
            const response = await api.get(`/Users/${userId}`);
            return response.data;
        } catch (error) {
            console.error(`Error fetching user ${userId}:`, error);
            throw error;
        }
    },

    /**
     * Create a new backoffice or grid operator user
     */
    createUser: async (userData) => {
        try {
            const response = await api.post('/Users', userData);
            return response.data;
        } catch (error) {
            console.error('Error creating user:', error);
            throw error;
        }
    },

    /**
     * Update an existing user
     */
    updateUser: async (userId, userData) => {
        try {
            const response = await api.put(`/Users/${userId}`, userData);
            return response.data;
        } catch (error) {
            console.error(`Error updating user ${userId}:`, error);
            throw error;
        }
    }
};

export default userService;
