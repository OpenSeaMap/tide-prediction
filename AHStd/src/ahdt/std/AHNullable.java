/*    
    Copyright (C) 2013 by Alexej Humbach.

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ahdt.std;

/**
 * 
 * @author humbach
 * @version 1.0
 * 
 */
public class AHNullable
{
	private boolean m_bIsNull;

	public AHNullable()
	{
		m_bIsNull = true;
	}

	protected AHNullable(boolean isNull)
	{
		this.m_bIsNull = isNull;
	}

	public boolean isNull()
	{
		return m_bIsNull;
	}

	public boolean isNotNull()
	{
		return !m_bIsNull;
	}

	public void makeNull()
	{
		m_bIsNull = true;
	}

	public void makeNull(boolean nullIt)
	{
		this.m_bIsNull = nullIt;
	}
}
