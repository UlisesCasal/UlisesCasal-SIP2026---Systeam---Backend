package com.systeam.backend.auth.security;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
//EN CADA PETICION, VERIFICA EL TOKEN Y EL USUARIO
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    // Link con JwtService
    private final JwtService jwtService;
    // Link con CustomUserDetailsService para obtener el UserDetails
    private final CustomUserDetailsService customUserDetailsService;

    //Metodo que recibe una solicitud y la filtra
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        
        //Obtiene el header de autenticacion
        final String authHeader = request.getHeader("Authorization");
        
        //Si no hay header de autenticacion o no comienza con Bearer, no se hace nada
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // Continúa con el siguiente filtro en la cadena sin procesar esta solicitud
            filterChain.doFilter(request, response);
            return;
        }
        //Obtiene el token del header y el email asociado al token
        String token = authHeader.substring(7);
        String email = jwtService.extractUsername(token);
        
        //Si el email es diferente de null y el usuario no está autenticado, verifica el token
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

            if (jwtService.isTokenValid(token, userDetails)) {
                //Si el token es válido, se crea un UsernamePasswordAuthenticationToken
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities());
                //Se establece los detalles del usuario en el token
                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request));
                //Se establece el usuario autenticado en el SecurityContextHolder para que se recuerde
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        // Pasar la solicitud y la respuesta al siguiente filtro en la cadena
        // Esto asegura que el flujo de la aplicación continúe después del procesamiento JWT
        filterChain.doFilter(request, response);
    }

}
