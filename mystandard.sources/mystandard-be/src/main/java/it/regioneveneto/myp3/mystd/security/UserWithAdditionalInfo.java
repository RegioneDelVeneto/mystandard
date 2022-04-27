/**
 *     My Standard
 *     Copyright (C) 2022  Regione Veneto
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as
 *     published by the Free Software Foundation, either version 3 of the
 *     License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package it.regioneveneto.myp3.mystd.security;

import it.regioneveneto.myp3.mystd.bean.enumeration.MyStandardRoleEnum;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserWithAdditionalInfo implements UserDetails {

	private final String username;
	private final String nome;
	private final String cognome;
	private final String codiceFiscale;
	private final String email;
	private final String telefono;
	private List<String> roles;
	private List<String> domains;
	private String ipa;
	private final List<ProfileUser> userProfiles;
	private Boolean ipaScelto = false;

	private final Long userId;

	private List<GrantedAuthority> grantedAuths;

	public UserWithAdditionalInfo(String username, String nome, String cognome, String codiceFiscale, String email,
			String telefono, Long userId, List<String> roles, List<String> domains, String ipa, Boolean ipaScelto, List<ProfileUser> userProfiles, List<GrantedAuthority> grantedAuths) {
		super();
		this.username = username;
		this.nome = nome;
		this.cognome = cognome;
		this.codiceFiscale = codiceFiscale;
		this.email = email;
		this.telefono = telefono;
		this.userId = userId;
		this.roles = roles;
		this.domains = domains;
		this.ipa = ipa;
		this.userProfiles = userProfiles;
		this.grantedAuths = grantedAuths;
		this.ipaScelto = checkIpa(ipaScelto, userProfiles);
	}



	public String getUsername() {
		return username;
	}

	public String getNome() {
		return nome;
	}

	public String getCognome() {
		return cognome;
	}

	public String getCodiceFiscale() {
		return codiceFiscale;
	}

	public String getEmail() {
		return email;
	}

	public String getTelefono() {
		return telefono;
	}

	public Long getUserId() {
		return userId;
	}

	public List<String> getRoles() {
		return roles;
	}

	public List<String> getDomains() {
		return domains;
	}

	public List<ProfileUser> getUserProfiles() {
		return userProfiles;
	}

	public Boolean getIpaScelto() {
		return ipaScelto;
	}

	public void setIpaScelto(Boolean ipaScelto) {
		this.ipaScelto = ipaScelto;
	}

	public void setRoles(List<String> roles) {
		this.roles = roles;
	}

	public void setDomains(List<String> domains) {
		this.domains = domains;
	}

	public String getIpa() {
		return ipa;
	}

	public void setIpa(String ipa) {
		this.ipa = ipa;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return grantedAuths;
	}

	@Override
	public String getPassword() {
		return "$2a$10$Y8zuzAtbwHbVacVLLBK/ve3Fb0veCvNBJppMUG9XtOLky5SaGLyHq";
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}



	public String getMystandardUsername() {
		return this.nome + " " + this.cognome;
	}

	public Boolean isOperatoreEnteLocale() {
		return this.roles.contains(MyStandardRoleEnum.OPERATORE_ENTE_LOCALE.getRole());
	}

	public Boolean isOperatoreEnteNazionale() {
		return this.roles.contains(MyStandardRoleEnum.OPERATORE_ENTE_NAZIONALE.getRole());
	}

	public Boolean isResponsabileDominio() {
		return this.roles.contains(MyStandardRoleEnum.RESPONSABILE_DOMINIO.getRole());
	}

	public Boolean isResponsabileStandard() {
		return this.roles.contains(MyStandardRoleEnum.RESPONSABILE_STANDARD.getRole());
	}

	/*
	Logica per la scelta da parte dell'utente con che ipa operare
	 */
	private Boolean checkIpa(Boolean ipaScelto, List<ProfileUser> userProfiles) {
		if (this.ipaScelto || ipaScelto) {
			return true;//Utente ha già scelto con che ipa operare
		} else {
			if (userProfiles != null && userProfiles.size() == 1) {
				this.ipa = userProfiles.get(0).getIpa();
				return true;//Resp o ope nazionale => un solo IPA non serve scegliere
			} else {
				return false;
			}
		}
	}

	public Map<String, Object> getClaims() {
		Map<String, Object> claims = new HashMap<>();
		claims.put("cognome",this.getCognome());
		claims.put("nome",this.getNome());
		claims.put("codiceFiscale",this.getCodiceFiscale());
		claims.put("email",this.getEmail());
		claims.put("profile", this.getUserProfiles());
		claims.put("roles", this.getRoles());
		claims.put("ipa", this.getIpa());
		claims.put("domains", this.getDomains());
		claims.put("ipaScelto", this.getIpaScelto());


		return claims;
	}
}
