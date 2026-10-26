import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { BrowserRouter } from 'react-router-dom';
import Login from '../Login';
import * as authApi from '../../api/auth';

vi.mock('../../api/auth');

const mockNavigate = vi.fn();
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

describe('Login Page', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  const renderLogin = () => {
    return render(
      <BrowserRouter>
        <Login />
      </BrowserRouter>
    );
  };

  it('affiche le formulaire de connexion', () => {
    renderLogin();

    expect(screen.getByPlaceholderText(/email/i)).toBeInTheDocument();
    expect(screen.getByPlaceholderText(/mot de passe/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /connexion/i })).toBeInTheDocument();
  });

  it('affiche le logo SEWS', () => {
    renderLogin();
    
    const logo = screen.getByAltText(/SEWS CABIND MAROC/i);
    expect(logo).toBeInTheDocument();
  });

  it('valide que les champs sont requis', async () => {
    const user = userEvent.setup();
    renderLogin();

    const submitButton = screen.getByRole('button', { name: /connexion/i });
    await user.click(submitButton);

    // Le formulaire HTML5 empêche la soumission si les champs requis sont vides
    const emailInput = screen.getByPlaceholderText(/email/i);
    const passwordInput = screen.getByPlaceholderText(/mot de passe/i);
    
    expect(emailInput).toBeRequired();
    expect(passwordInput).toBeRequired();
  });

  it('soumet les credentials et redirige vers le dashboard', async () => {
    const user = userEvent.setup();
    const mockResponse = {
      token: 'test-token',
      email: 'admin@test.com',
      fullName: 'Admin User',
      role: 'ADMIN',
    };

    authApi.login = vi.fn().mockResolvedValue(mockResponse);

    renderLogin();

    const emailInput = screen.getByPlaceholderText(/email/i);
    const passwordInput = screen.getByPlaceholderText(/mot de passe/i);
    const submitButton = screen.getByRole('button', { name: /connexion/i });

    await user.type(emailInput, 'admin@test.com');
    await user.type(passwordInput, 'password123');
    await user.click(submitButton);

    await waitFor(() => {
      expect(authApi.login).toHaveBeenCalledWith('admin@test.com', 'password123');
    });

    await waitFor(() => {
      expect(mockNavigate).toHaveBeenCalledWith('/');
    });
  });

  it('affiche une erreur en cas d\'échec de connexion', async () => {
    const user = userEvent.setup();
    
    authApi.login = vi.fn().mockRejectedValue({
      response: {
        data: { error: 'Identifiants incorrects' },
        status: 401,
      },
    });

    renderLogin();

    const emailInput = screen.getByPlaceholderText(/email/i);
    const passwordInput = screen.getByPlaceholderText(/mot de passe/i);
    const submitButton = screen.getByRole('button', { name: /connexion/i });

    await user.type(emailInput, 'wrong@test.com');
    await user.type(passwordInput, 'wrongpass');
    await user.click(submitButton);

    await waitFor(() => {
      expect(screen.getByText(/identifiants incorrects/i)).toBeInTheDocument();
    });
  });

  it('désactive le bouton pendant la soumission', async () => {
    const user = userEvent.setup();
    
    authApi.login = vi.fn().mockImplementation(() => 
      new Promise(resolve => setTimeout(resolve, 1000))
    );

    renderLogin();

    const emailInput = screen.getByPlaceholderText(/email/i);
    const passwordInput = screen.getByPlaceholderText(/mot de passe/i);
    const submitButton = screen.getByRole('button', { name: /connexion/i });

    await user.type(emailInput, 'admin@test.com');
    await user.type(passwordInput, 'password123');
    await user.click(submitButton);

    expect(submitButton).toBeDisabled();
  });

  it('permet d\'afficher/masquer le mot de passe', async () => {
    const user = userEvent.setup();
    renderLogin();

    const passwordInput = screen.getByPlaceholderText(/mot de passe/i);
    expect(passwordInput).toHaveAttribute('type', 'password');

    const toggleButton = screen.getByRole('button', { name: '' }); // Bouton œil
    await user.click(toggleButton);

    expect(passwordInput).toHaveAttribute('type', 'text');

    await user.click(toggleButton);
    expect(passwordInput).toHaveAttribute('type', 'password');
  });
});
